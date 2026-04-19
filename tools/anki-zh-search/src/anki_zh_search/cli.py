from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any

ANKI_BASE = Path.home() / "Library" / "Application Support" / "Anki2"
DEFAULT_DECK = "Chinese"

QUEUE_LABEL = {
    -3: "buried(user)",
    -2: "buried(sched)",
    -1: "suspended",
    0: "new",
    1: "learning",
    2: "review",
    3: "day-learning",
    4: "preview",
}

TYPE_LABEL = {0: "new", 1: "learning", 2: "review", 3: "relearning"}

TITLE_FIELD_CANDIDATES = (
    "Hanzi",
    "Simplified",
    "Simplified Chinese",
    "Chinese",
    "Word",
    "Expression",
    "Front",
)

_HTML_TAG_RE = re.compile(r"<[^>]+>")
_SOUND_RE = re.compile(r"\[sound:[^\]]+\]")
_WS_RE = re.compile(r"[ \t]+")


def find_collection(profile: str | None) -> Path:
    if not ANKI_BASE.exists():
        sys.exit(f"Anki data directory not found: {ANKI_BASE}")

    if profile:
        candidate = ANKI_BASE / profile / "collection.anki2"
        if not candidate.exists():
            sys.exit(f"No collection for profile '{profile}': {candidate}")
        return candidate

    profiles = [
        p for p in ANKI_BASE.iterdir()
        if p.is_dir() and (p / "collection.anki2").exists()
    ]
    if not profiles:
        sys.exit(f"No Anki profile found under {ANKI_BASE}")
    if len(profiles) > 1:
        names = ", ".join(sorted(p.name for p in profiles))
        sys.exit(f"Multiple profiles found ({names}); pass --profile to pick one")
    return profiles[0] / "collection.anki2"


def strip_html(text: str) -> str:
    text = _SOUND_RE.sub("♪", text)
    text = _HTML_TAG_RE.sub(" ", text)
    text = (
        text.replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&#39;", "'")
        .replace("&quot;", '"')
    )
    text = _WS_RE.sub(" ", text)
    return text.strip()


def card_summary(card) -> dict[str, Any]:
    return {
        "id": int(card.id),
        "type": TYPE_LABEL.get(card.type, str(card.type)),
        "type_raw": int(card.type),
        "queue": QUEUE_LABEL.get(card.queue, str(card.queue)),
        "queue_raw": int(card.queue),
        "interval_days": int(card.ivl),
        "ease_permille": int(card.factor),
        "reps": int(card.reps),
        "lapses": int(card.lapses),
        "due": int(card.due),
    }


def build_query(raw: str, deck: str, all_decks: bool) -> str:
    raw = raw.strip()
    if all_decks:
        return raw
    return f'deck:"{deck}" ({raw})' if raw else f'deck:"{deck}"'


def search(col_path: Path, query: str, limit: int) -> tuple[list[dict], int]:
    from anki.collection import Collection

    try:
        col = Collection(str(col_path))
    except Exception as exc:
        message = str(exc).lower()
        if "lock" in message or "busy" in message:
            sys.exit("Anki database is locked. Close the Anki desktop app and retry.")
        raise

    try:
        nids = list(col.find_notes(query))
        total = len(nids)
        nids = nids[:limit]

        results: list[dict] = []
        for nid in nids:
            note = col.get_note(nid)
            note_type = note.note_type()
            fields = {name: value for name, value in zip(note.keys(), note.values())}
            cards = [card_summary(c) for c in note.cards()]
            results.append({
                "note_id": int(nid),
                "note_type": note_type["name"] if note_type else "?",
                "tags": list(note.tags),
                "fields": fields,
                "cards": cards,
            })
        return results, total
    finally:
        col.close()


def emit_json(results: list[dict], query: str, total: int) -> None:
    payload = {
        "query": query,
        "total_matches": total,
        "returned": len(results),
        "notes": results,
    }
    json.dump(payload, sys.stdout, ensure_ascii=False, indent=2)
    sys.stdout.write("\n")


def _pick_title(fields: dict[str, str]) -> str:
    for key in TITLE_FIELD_CANDIDATES:
        if key in fields and fields[key].strip():
            return strip_html(fields[key])
    for value in fields.values():
        clean = strip_html(value)
        if clean:
            return clean
    return "(empty)"


def _format_cards(cards: list[dict]) -> str:
    if not cards:
        return "no cards"
    parts = []
    for c in cards:
        bits = [c["type"]]
        if c["queue_raw"] < 0:
            bits.append(c["queue"])
        if c["interval_days"]:
            bits.append(f"ivl {c['interval_days']}d")
        if c["reps"]:
            bits.append(f"reps {c['reps']}")
        if c["lapses"]:
            bits.append(f"lapses {c['lapses']}")
        parts.append(" ".join(bits))
    return " │ ".join(parts)


def emit_rich(results: list[dict], query: str, total: int) -> None:
    from rich.console import Console
    from rich.panel import Panel
    from rich.table import Table
    from rich.text import Text

    console = Console()
    if not results:
        console.print(f"[yellow]No matches for[/yellow] [bold]{query}[/bold]")
        return

    header = (
        f"[bold]{len(results)}[/bold] of [bold]{total}[/bold] match(es)  "
        f"[dim]query:[/dim] [cyan]{query}[/cyan]"
    )
    console.print(header)
    console.print()

    for note in results:
        title_line = Text()
        title_line.append(_pick_title(note["fields"]), style="bold cyan")
        title_line.append(f"   {note['note_type']}", style="dim")
        title_line.append(f"   #{note['note_id']}", style="dim")

        field_table = Table.grid(padding=(0, 2))
        field_table.add_column(style="bold yellow", no_wrap=True, vertical="top")
        field_table.add_column(overflow="fold")
        for name, raw in note["fields"].items():
            clean = strip_html(raw)
            if not clean:
                field_table.add_row(name, Text("(empty)", style="dim"))
            else:
                field_table.add_row(name, clean)

        body = Table.grid(padding=(0, 0))
        body.add_column()
        body.add_row(field_table)
        body.add_row(Text(_format_cards(note["cards"]), style="green"))
        if note["tags"]:
            body.add_row(Text("tags: " + " ".join(note["tags"]), style="magenta"))

        console.print(Panel(body, title=title_line, title_align="left", border_style="blue"))


def main() -> None:
    parser = argparse.ArgumentParser(
        prog="anki-zh",
        description="Search the Chinese deck in your local Anki collection.",
    )
    parser.add_argument(
        "query",
        nargs="+",
        help="search query (Anki syntax supported, e.g. '学习' or 'tag:hsk4 *学*')",
    )
    parser.add_argument("--deck", default=DEFAULT_DECK, help=f"deck name (default: {DEFAULT_DECK})")
    parser.add_argument("--all-decks", action="store_true", help="search all decks, not just --deck")
    parser.add_argument("--limit", type=int, default=20, help="max results (default: 20)")
    parser.add_argument("--json", dest="as_json", action="store_true", help="emit JSON instead of rich output")
    parser.add_argument("--profile", default=None, help="Anki profile name (auto-detected if only one)")
    parser.add_argument("--collection", type=Path, default=None, help="explicit path to collection.anki2")
    args = parser.parse_args()

    col_path = args.collection or find_collection(args.profile)
    raw_query = " ".join(args.query)
    query = build_query(raw_query, args.deck, args.all_decks)

    results, total = search(col_path, query, args.limit)

    if args.as_json:
        emit_json(results, query, total)
    else:
        emit_rich(results, query, total)


if __name__ == "__main__":
    main()
