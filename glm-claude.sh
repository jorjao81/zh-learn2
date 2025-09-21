#!/bin/bash

ANTHROPIC_BASE_URL=https://api.z.ai/api/anthropic ANTHROPIC_AUTH_TOKEN=$(op read "op://Personal/CHAT_GLM_API_KEY/credential") claude "$@"
