## Input Format
You will receive:
- **Chinese word**: The multi-character Chinese word/phrase being defined
- **Original content (optional)**: The raw definition/meaning from Pleco export

Format the content so it looks like the exemples:

```html 
<span class="part-of-speech">noun</span> snare; trap
``` 

```html 
<span class="part-of-speech">verb</span> <span class="usage">colloquial</span>
<ol>
    <li>boast; brag; talk big</li>
    <li><span class="usage dialect">dialect</span> chat; talk casually</li>
</ol>
```html 

<span class="part-of-speech">noun</span> <span class="domain">anatomy</span> vein
``` 


```html 
<span class="part-of-speech">verb</span> 
<ol>
    <li>entice; tempt; seduce; lure</li>
    <li>attract; allure</li>
</ol>
``` 

```html 
<span class="part-of-speech">noun</span>
<ol>
    <li> <span class="domain">medicine</span> miscarriage; abortion</li>
</ol>
<span class="part-of-speech">verb</span>
<ol>
    <li><span class="domain">medicine</span> (of a woman) have a miscarriage; miscarry; abort</li>
    <li><span class="usage">figurative</span> (of a plan, etc.) miscarry; fall through; abort</li>
</ol>
``` 


if a usage or domain is in parentheses, remove the parentheses, eg:

input:
```
Chinese Word: 高维
Original Content: (math.) higher dimensional
```

desired output:
<span class="domain">mathematics</span> higher dimensional

If you receive Original Content (i.e, a pleco dictionary definition):
Do not add anything that changes meaning, just add the formatting and expand abbreviations.

If you DO NOT receive Original Content (and only in that case), create a definition in the style above yourself.

Provide correct well formed html snippets in the response. 


## Common domain words (non exhaustive)
ecology
anatomy
pharmacy
linguistics
mathematics