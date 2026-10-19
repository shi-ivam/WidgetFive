#!/usr/bin/env python3
"""Generate or execute bounded Command Code widget-import passes for Nothing Widgets.

Target APK: MyNothingWidgets.apkm (Nothing Widgets 3.5.4, com.nothing.hearthstone)

By default this program is a dry run: it prints each command without running
anything. Add --execute only after reviewing the generated invocation list.
"""

from __future__ import annotations

import argparse
import json
import math
import os
import shlex
import subprocess
import sys
import time
from pathlib import Path
from typing import Any


DEFAULT_MODEL = os.environ.get("COMMANDCODE_MODEL", "xiaomi/mimo-v2.6-pro")
CHECKPOINT_FILE = ".orchestrator-checkpoint.json"
INVENTORY_FILE = "widget_inventory.json"

# Complete catalog of all 32 widget card providers from Nothing Widgets 3.5.4 (com.nothing.hearthstone)
WIDGET_CATALOG: list[dict[str, Any]] = [
    {
        "id": 1,
        "name": "Analog Clock",
        "category": "Clock",
        "provider_class": "com.nothing.hearthstone.clock.provider.AnalogWidgetProvider",
        "authority": "com.nothing.hearthstone.authority.APP_CARD.analog_clock",
        "layouts": [
            "card_analog_clock_bold_1x1.xml",
            "card_analog_clock_bold_2x2.xml",
            "card_analog_clock_scale_1x1.xml",
            "card_analog_clock_scale_2x2.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.clock.ui.AnalogClockConfigureActivity",
        "description": "Analog clock with bold and scale face designs (1x1 and 2x2 sizes, simple and detailed views).",
    },
    {
        "id": 2,
        "name": "Digital Clock",
        "category": "Clock",
        "provider_class": "com.nothing.hearthstone.clock.provider.DigitalWidgetProvider",
        "authority": "com.nothing.hearthstone.authority.APP_CARD.digital_clock",
        "layouts": [
            "card_digital_clock_1_by_1.xml",
            "card_digital_clock_1_by_2.xml",
            "card_digital_clock_2_by_1.xml",
        ],
        "configure_activity": None,
        "description": "Digital clock with dot-matrix typography (1x1, 1x2, 2x1 sizes, normal and simple variants).",
    },
    {
        "id": 3,
        "name": "World Clock",
        "category": "Clock",
        "provider_class": "com.nothing.hearthstone.clock.provider.WorldWidgetProvider",
        "authority": "com.nothing.hearthstone.authority.APP_CARD.world_clock",
        "layouts": [
            "card_world_clock_1x1.xml",
            "card_world_clock_1x2.xml",
            "card_world_clock_2x1.xml",
            "card_world_clock_2x2.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.clock.ui.WorldClockConfigureActivity",
        "description": "Dual and multi-timezone world clock with city search (1x1, 1x2, 2x1, 2x2 sizes).",
    },
    {
        "id": 4,
        "name": "QuickLook (Transparent)",
        "category": "QuickLook",
        "provider_class": "com.nothing.hearthstone.quicklook.QLTransparentWidgetProvider",
        "authority": "com.nothing.hearthstone.authority.APP_CARD.ql_transparent",
        "layouts": [
            "card_date_14u_layout.xml",
            "card_date_aod_14u_layout.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.quicklook.QuickLookSettingsActivity",
        "description": "Glanceable weather, upcoming calendar events, and notifications with transparent background.",
    },
    {
        "id": 5,
        "name": "QuickLook (Card Mode)",
        "category": "QuickLook",
        "provider_class": "com.nothing.hearthstone.quicklook.QLCardModeWidgetProvider",
        "authority": "com.nothing.hearthstone.authority.APP_CARD.ql_card",
        "layouts": [
            "card_date_layout.xml",
            "card_date_aod_layout.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.quicklook.QuickLookSettingsActivity",
        "description": "Glanceable weather, upcoming calendar events, and notifications with solid card background.",
    },
    {
        "id": 6,
        "name": "Photo Widget (Rectangle)",
        "category": "Photo",
        "provider_class": "com.nothing.hearthstone.photo.provider.PhotoWidgetRectangleProvider",
        "authority": "com.nothing.hearthstone.authority.PHOTO.rectangle",
        "layouts": [
            "card_photo_rectangle_1x1.xml",
            "card_photo_rectangle_2x1.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.photo.ui.RectanglePhotoSettingActivity",
        "description": "Rounded rectangular frame photo showcase supporting single or carousel image rotation (1x1, 2x1).",
    },
    {
        "id": 7,
        "name": "Photo Widget (Circle)",
        "category": "Photo",
        "provider_class": "com.nothing.hearthstone.photo.provider.PhotoWidgetCircleProvider",
        "authority": "com.nothing.hearthstone.authority.PHOTO.circle",
        "layouts": [
            "card_photo_circle_1x1.xml",
            "card_photo_circle_2x1.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.photo.ui.CirclePhotoSettingActivity",
        "description": "Circular crop photo showcase supporting gallery selection and framing (1x1, 2x1).",
    },
    {
        "id": 8,
        "name": "Compass (2x2)",
        "category": "Compass",
        "provider_class": "com.nothing.hearthstone.compass.CompassProvider",
        "authority": "com.nothing.hearthstone.authority.compass",
        "layouts": [
            "widget_compass.xml",
            "widget_compass_aod.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.compass.config.CompassSettingsActivity",
        "description": "Full 2x2 interactive compass dial with heading degree, cardinal direction, and elevation.",
    },
    {
        "id": 9,
        "name": "Small Compass (1x1)",
        "category": "Compass",
        "provider_class": "com.nothing.hearthstone.compass.provider.SmallCompassProvider",
        "authority": "com.nothing.hearthstone.authority.compass.small",
        "layouts": [
            "widget_item_compass_1x1.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.compass.config.CompassSettingsActivity",
        "description": "Minimal 1x1 compass indicator showing cardinal needle and current heading.",
    },
    {
        "id": 10,
        "name": "Pedometer Main (2x2)",
        "category": "Pedometer",
        "provider_class": "com.nothing.hearthstone.pedometer.PedometerProvider",
        "authority": "com.nothing.hearthstone.authority.pedometer",
        "layouts": [
            "widget_pedometer_plugin.xml",
            "widget_pedometer_plugin_aod.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.pedometer.PedometerSettingsActivity",
        "description": "Full 2x2 fitness tracker displaying daily step count, distance, calories, and progress ring.",
    },
    {
        "id": 11,
        "name": "Pedometer Progress Card (2x1)",
        "category": "Pedometer",
        "provider_class": "com.nothing.hearthstone.pedometer.WidgetProgressProvider",
        "authority": "com.nothing.hearthstone.authority.pedometer.progress",
        "layouts": [
            "widget_pedometer_progress_card.xml",
            "widget_pedometer_progress_card_aod.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.pedometer.PedometerSettingsActivity",
        "description": "2x1 compact progress widget showing circular step progress toward daily goal.",
    },
    {
        "id": 12,
        "name": "Pedometer Daily Count Card (2x1)",
        "category": "Pedometer",
        "provider_class": "com.nothing.hearthstone.pedometer.WidgetDailyCountProvider",
        "authority": "com.nothing.hearthstone.authority.pedometer.dailyCount",
        "layouts": [
            "widget_daily_count_card.xml",
            "widget_daily_count_card_aod.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.pedometer.PedometerSettingsActivity",
        "description": "2x1 numeric step card highlighting total daily steps taken.",
    },
    {
        "id": 13,
        "name": "Pedometer Weekly Streak Card (2x1)",
        "category": "Pedometer",
        "provider_class": "com.nothing.hearthstone.pedometer.WidgetWeeklyStreakProvider",
        "authority": "com.nothing.hearthstone.authority.pedometer.weeklyStreak",
        "layouts": [
            "widget_weekly_streak_card.xml",
            "widget_weekly_streak_card_aod.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.pedometer.PedometerSettingsActivity",
        "description": "2x1 7-day bar chart showing daily goal achievements and active streak.",
    },
    {
        "id": 14,
        "name": "Community Widget",
        "category": "Community",
        "provider_class": "com.nothing.hearthstone.community.ui.CommunityWidgetProvider",
        "authority": "com.nothing.hearthstone.authority.community",
        "layouts": [
            "widget_community_default.xml",
            "widget_community_default_2x4.xml",
        ],
        "configure_activity": None,
        "description": "Nothing Community updates, featured community posts, and announcements (2x2, 2x4).",
    },
    {
        "id": 15,
        "name": "Screen Time Main (2x2)",
        "category": "Screen Time",
        "provider_class": "com.nothing.hearthstone.screentime.provider.ScreenTimeProvider",
        "authority": "com.nothing.hearthstone.authority.SCREEN_TIME",
        "layouts": [
            "widget_screen_time.xml",
            "widget_screen_aod_time.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.screentime.ui.ScreenTimeSettingActivity",
        "description": "2x2 digital wellbeing monitor showing app usage breakdown, total screen time, and unlock count.",
    },
    {
        "id": 16,
        "name": "Screen Time Duration Card (2x1)",
        "category": "Screen Time",
        "provider_class": "com.nothing.hearthstone.screentime.provider.MiddleDurationScreenTimeProvider",
        "authority": "com.nothing.hearthstone.authority.SCREEN_TIME_2_1_DURATION",
        "layouts": [
            "widget_screen_time_duration.xml",
            "widget_screen_aod_time_duration.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.screentime.ui.ScreenTimeSettingActivity",
        "description": "2x1 horizontal bar chart comparing today's screen time against average or limits.",
    },
    {
        "id": 17,
        "name": "Screen Time Shape Card (2x1)",
        "category": "Screen Time",
        "provider_class": "com.nothing.hearthstone.screentime.provider.MiddleShapeScreenTimeProvider",
        "authority": "com.nothing.hearthstone.authority.SCREEN_TIME_2_1_SHAPE",
        "layouts": [
            "widget_screen_time_shape.xml",
            "widget_screen_aod_time_shape.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.screentime.ui.ScreenTimeSettingActivity",
        "description": "2x1 geometric shape visualization that morphs based on screen time consumption.",
    },
    {
        "id": 18,
        "name": "Screen Time Expression Card (1x1)",
        "category": "Screen Time",
        "provider_class": "com.nothing.hearthstone.screentime.provider.SmallScreenTimeProvider",
        "authority": "com.nothing.hearthstone.authority.SCREEN_TIME_1_1_EXPRESSION",
        "layouts": [
            "widget_screen_time_expression.xml",
            "widget_screen_aod_time_expression.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.screentime.ui.ScreenTimeSettingActivity",
        "description": "1x1 emotional expression avatar (happy/neutral/tired) reflecting daily device usage levels.",
    },
    {
        "id": 19,
        "name": "Focus Music (2x2)",
        "category": "Scene Music",
        "provider_class": "com.nothing.hearthstone.scenemusic.provider.FocusMusicProvider",
        "authority": "com.nothing.hearthstone.authority.focus_music",
        "layouts": [
            "card_focus_music.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.scenemusic.ui.SceneMusicSettingsActivity",
        "description": "Ambient generative soundscapes optimized for deep work, study, and focus (2x2).",
    },
    {
        "id": 20,
        "name": "Calm Rest Music (2x2)",
        "category": "Scene Music",
        "provider_class": "com.nothing.hearthstone.scenemusic.provider.CalmRestMusicProvider",
        "authority": "com.nothing.hearthstone.authority.calm_rest",
        "layouts": [
            "card_calm_rest_music.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.scenemusic.ui.SceneMusicSettingsActivity",
        "description": "Relaxing ambient audio synthesizer designed for meditation and breaks (2x2).",
    },
    {
        "id": 21,
        "name": "Sleep Music (2x2)",
        "category": "Scene Music",
        "provider_class": "com.nothing.hearthstone.scenemusic.provider.SleepMusicProvider",
        "authority": "com.nothing.hearthstone.authority.sleep_music",
        "layouts": [
            "card_deep_sleep_music.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.scenemusic.ui.SceneMusicSettingsActivity",
        "description": "Soothing white/pink noise and delta-wave sleep audio generator with sleep timer (2x2).",
    },
    {
        "id": 22,
        "name": "Date Card",
        "category": "Date",
        "provider_class": "com.nothing.hearthstone.date.provider.DateProvider",
        "authority": "com.nothing.hearthstone.authority.DATE",
        "layouts": [
            "card_date_layout.xml",
            "card_date_14u_layout.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.date.ui.DateSettingActivity",
        "description": "Stylized date block with dot-matrix day/month numerals and upcoming agenda items.",
    },
    {
        "id": 23,
        "name": "Media Player (2x2)",
        "category": "Media Player",
        "provider_class": "com.nothing.hearthstone.mediaplayer.business.MediaPlayerProvider",
        "authority": "com.nothing.hearthstone.authority.APP_CARD.media_player",
        "layouts": [
            "card_media_player_2x2.xml",
        ],
        "configure_activity": None,
        "description": "Interactive music/podcast media player with album artwork, playback controls, and track progress (2x2).",
    },
    {
        "id": 24,
        "name": "Battery (2x2)",
        "category": "Battery",
        "provider_class": "com.nothing.hearthstone.battery.business.BatteryProvider",
        "authority": "com.nothing.hearthstone.authority.battery.big",
        "layouts": [
            "battery_container.xml",
            "widget_battery_aod.xml",
        ],
        "configure_activity": None,
        "description": "Multi-device power hub displaying phone battery and connected Bluetooth peripherals/Nothing Ear buds (2x2).",
    },
    {
        "id": 25,
        "name": "Small Battery (1x1)",
        "category": "Battery",
        "provider_class": "com.nothing.hearthstone.battery.business.SmallBatteryProvider",
        "authority": "com.nothing.hearthstone.authority.battery.small",
        "layouts": [
            "battery_small_container.xml",
            "widget_battery_1x1_aod.xml",
        ],
        "configure_activity": None,
        "description": "Compact single gauge showing phone or selected device battery percentage (1x1).",
    },
    {
        "id": 26,
        "name": "ChatGPT Scrollable (2x2)",
        "category": "ChatGPT",
        "provider_class": "com.nothing.hearthstone.chatgpt.provider.GPTScrollableProvider",
        "authority": "com.nothing.hearthstone.authority.chatgpt.scrollable",
        "layouts": [
            "widget_chatgpt_2x2_scrollable.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.chatgpt.ui.GPTScrollableConfigureActivity",
        "description": "2x2 interactive AI prompt widget with scrollable conversation snippets and voice query launcher.",
    },
    {
        "id": 27,
        "name": "ChatGPT Static",
        "category": "ChatGPT",
        "provider_class": "com.nothing.hearthstone.chatgpt.provider.GPTStaticProvider",
        "authority": "com.nothing.hearthstone.authority.chatgpt.static",
        "layouts": [
            "widget_chatgpt_1x1.xml",
            "widget_chatgpt_1x2.xml",
            "widget_chatgpt_2x2.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.chatgpt.ui.GPTStaticConfigureActivity",
        "description": "Quick launcher buttons for ChatGPT voice and text prompt inputs (1x1, 1x2, 2x2).",
    },
    {
        "id": 28,
        "name": "News Reporter (2x2)",
        "category": "News",
        "provider_class": "com.nothing.hearthstone.news.provider.NewsReporterProvider",
        "authority": "com.nothing.hearthstone.authority.news",
        "layouts": [
            "widget_news_2x2.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.news.ui.NewsReporterSettingActivity",
        "description": "Curated daily news summary and automated voice read-aloud widget (2x2).",
    },
    {
        "id": 29,
        "name": "Countdown Timer (2x2)",
        "category": "Countdown",
        "provider_class": "com.nothing.hearthstone.countdown.provider.CountdownProvider",
        "authority": "com.nothing.hearthstone.authority.countdown",
        "layouts": [
            "card_countdown_2x2.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.countdown.ui.CountdownSettingActivity",
        "description": "Event countdown timer tracking remaining days/hours/minutes to important milestone dates (2x2).",
    },
    {
        "id": 30,
        "name": "Breath - Relax (2x2)",
        "category": "Breath",
        "provider_class": "com.nothing.hearthstone.breath.provider.BreathRelaxProvider",
        "authority": "com.nothing.hearthstone.authority.BREATH.relax",
        "layouts": [
            "card_breath_relax.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.breath.ui.BreathSettingActivity",
        "description": "Guided relaxing breathing pattern widget with visual expanding/contracting lung animations (2x2).",
    },
    {
        "id": 31,
        "name": "Breath - Focus (2x2)",
        "category": "Breath",
        "provider_class": "com.nothing.hearthstone.breath.provider.BreathFocusProvider",
        "authority": "com.nothing.hearthstone.authority.BREATH.focus",
        "layouts": [
            "card_breath_focus.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.breath.ui.BreathSettingActivity",
        "description": "Guided box-breathing and focus pattern exercise widget (2x2).",
    },
    {
        "id": 32,
        "name": "Breath - Calm (2x2)",
        "category": "Breath",
        "provider_class": "com.nothing.hearthstone.breath.provider.BreathCalmProvider",
        "authority": "com.nothing.hearthstone.authority.BREATH.calm",
        "layouts": [
            "card_breath_calm.xml",
        ],
        "configure_activity": "com.nothing.hearthstone.breath.ui.BreathSettingActivity",
        "description": "Guided calming and heart-rate reduction breathing exercise widget (2x2).",
    },
]

TOTAL_WIDGETS = len(WIDGET_CATALOG)


def find_target_apk(project_dir: Path) -> Path | None:
    """Find the target APK/APKM in the project directory."""
    apkm_files = sorted(project_dir.glob("*.apkm"))
    if apkm_files:
        return apkm_files[0]
    apk_files = [f for f in sorted(project_dir.glob("*.apk")) if not f.name.startswith("base")]
    if apk_files:
        return apk_files[0]
    base_apk = project_dir / "base.apk"
    if base_apk.exists():
        return base_apk
    return None


def ensure_widget_inventory(project_dir: Path) -> Path:
    """Ensure widget_inventory.json is written to disk for easy reference."""
    path = project_dir / INVENTORY_FILE
    if not path.exists():
        with open(path, "w", encoding="utf-8") as f:
            json.dump(WIDGET_CATALOG, f, indent=2)
            f.write("\n")
    return path


def build_widget_manifest(start: int, end: int) -> str:
    """Build a readable summary of assigned widgets in the batch."""
    lines: list[str] = []
    for item in WIDGET_CATALOG:
        if start <= item["id"] <= end:
            layouts_str = ", ".join(item.get("layouts", []))
            cfg_str = f" | Config: {item['configure_activity']}" if item.get("configure_activity") else ""
            lines.append(
                f"- [{item['id']:02d}] {item['name']} ({item['category']})\n"
                f"  Provider: `{item['provider_class']}`\n"
                f"  Authority: `{item['authority']}`{cfg_str}\n"
                f"  Key Layouts: {layouts_str}\n"
                f"  Purpose: {item['description']}"
            )
    return "\n".join(lines)


def build_prompt(
    start: int,
    end: int,
    loop: int = 1,
    target_apk: str = "MyNothingWidgets.apkm",
) -> str:
    """Build the widget extraction prompt tailored for Nothing Widgets."""
    manifest = build_widget_manifest(start, end)
    prompt = (
        f"/goal Extract and reconstruct Nothing OS widgets {start} to {end} from {target_apk}.\n\n"
        f"Source APK: {target_apk} (Nothing Widgets 3.5.4, package com.nothing.hearthstone)\n"
        f"Widget Batch: {start} through {end} (Pass {loop})\n\n"
        f"Assigned Widgets in this batch:\n"
        f"{manifest}\n\n"
        "Extraction & Implementation Instructions:\n"
        f"1. Source Extraction & Evidence:\n"
        f"   - Target file `{target_apk}` is in the current directory.\n"
        "   - If not already unpacked, extract `base.apk` from the `.apkm` bundle (e.g. `unzip -p MyNothingWidgets.apkm base.apk > base.apk`) and decompile with apktool or REA tools (`apktool d -f base.apk -o decompiled/` or `rea extract-artifact base.apk`).\n"
        "   - Use the decompiled layouts (`res/layout/card_*.xml`, `res/layout/widget_*.xml`), drawables, vectors, and font assets (`assets/fonts/ntype_82_*.otf`) as ground truth.\n\n"
        "2. Faithful One-to-One Reconstruction:\n"
        "   - Reconstruct each assigned widget's UI layout, dot-matrix typography, Nothing OS styling, sizing, and colors.\n"
        "   - Implement provider logic, PendingIntents, broadcast updates, background refresh, and configuration activities corresponding to each widget.\n"
        "   - In Nothing OS, widgets use the AppCard provider protocol (`com.nothing.hearthstone.authority.APP_CARD.*` / ContentProvider / RemoteViews). Reconstruct them faithfully into Android AppWidgetProviders or project card providers with identical layout and user interactions.\n"
        "   - Import all required transitive drawables, shapes, colors, and string resources.\n\n"
        "3. Scope & Workflow Rules:\n"
        "   - You are allowed to use subagents and do not stop until this batch is completely done.\n"
        "   - You are allowed to run passwordless sudo commands if extra tools or packages are needed.\n"
        "   - Skip writing or verifying unit tests. Focus strictly on complete layout, design fidelity, and functional behavior.\n"
        "   - Maintain high fidelity: verify rendered RemoteViews and layouts against the original APK XMLs.\n"
        "   - Work directly in the current repository; do not create unnecessary git branches. Push to main when done.\n"
    )
    if loop > 1:
        prompt += (
            "\nRefinement pass: Inspect for missing interactions, responsive layout scaling, edge cases, "
            "and add quality-of-life features expected by users.\n"
        )
    prompt += (
        f"\nThis invocation was generated by orchestrator.py for widgets {start}-{end}. "
        "Do not run or modify the orchestrator script; complete only the assigned widget batch.\n"
    )
    return prompt


# Legacy compatibility alias
PROMPT_TEMPLATE = build_prompt(1, 5, 1, "MyNothingWidgets.apkm")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=f"Run or print Command Code widget-import batches for Nothing Widgets (Total: {TOTAL_WIDGETS})."
    )
    parser.add_argument(
        "loops",
        type=int,
        help=f"Number of complete passes over all {TOTAL_WIDGETS} widgets.",
    )
    parser.add_argument(
        "widgets_per_run",
        type=int,
        help="Maximum number of consecutive widgets assigned to one Command Code run.",
    )
    parser.add_argument(
        "--start-widget",
        type=int,
        default=1,
        help=f"Starting widget index (default: 1, range 1-{TOTAL_WIDGETS}).",
    )
    parser.add_argument(
        "--end-widget",
        type=int,
        default=None,
        help=f"Ending widget index (default: {TOTAL_WIDGETS}).",
    )
    parser.add_argument(
        "--max-turns",
        type=int,
        default=100,
        help="Maximum turns for each Command Code run (default: 100).",
    )
    parser.add_argument(
        "--model",
        type=str,
        default=DEFAULT_MODEL,
        help=f"Model identifier for Command Code (default: {DEFAULT_MODEL}).",
    )
    parser.add_argument(
        "--effort",
        type=str,
        default=None,
        help="Reasoning effort level for Command Code (e.g. low, medium, high).",
    )
    parser.add_argument(
        "--execute",
        action="store_true",
        help="Actually run commands. Without this flag, print them only.",
    )
    parser.add_argument(
        "--resume",
        action="store_true",
        help="Resume from the saved pass and widget index after an interruption.",
    )
    parser.add_argument(
        "--max-retries",
        type=int,
        default=3,
        help="Maximum retries per batch upon transient failure or rate limit (default: 3).",
    )
    parser.add_argument(
        "--retry-delay",
        type=float,
        default=30.0,
        help="Base backoff delay in seconds for retrying failed batches (default: 30.0).",
    )
    parser.add_argument(
        "--delay-seconds",
        type=float,
        default=5.0,
        help="Pacing delay in seconds between consecutive batches to prevent rate limits (default: 5.0).",
    )
    return parser.parse_args()


def validate_args(args: argparse.Namespace) -> None:
    if args.loops < 1:
        raise ValueError("loops must be at least 1")
    if args.widgets_per_run < 1:
        raise ValueError("widgets_per_run must be at least 1")
    if args.max_turns < 1:
        raise ValueError("--max-turns must be at least 1")
    if args.start_widget < 1 or args.start_widget > TOTAL_WIDGETS:
        raise ValueError(f"--start-widget must be between 1 and {TOTAL_WIDGETS}")
    if args.end_widget is not None and (args.end_widget < args.start_widget or args.end_widget > TOTAL_WIDGETS):
        raise ValueError(f"--end-widget must be between {args.start_widget} and {TOTAL_WIDGETS}")
    if args.max_retries < 0:
        raise ValueError("--max-retries must be non-negative")
    if args.retry_delay < 0:
        raise ValueError("--retry-delay must be non-negative")
    if args.delay_seconds < 0:
        raise ValueError("--delay-seconds must be non-negative")
    if args.effort is not None and not args.effort.strip():
        raise ValueError("--effort must not be empty")


def command_for(
    start: int,
    end: int,
    max_turns: int,
    loop: int = 1,
    model: str = DEFAULT_MODEL,
    effort: str | None = None,
    target_apk: str = "MyNothingWidgets.apkm",
) -> list[str]:
    prompt = build_prompt(start, end, loop, target_apk)
    cmd = [
        "commandcode",
        "-p",
        prompt,
        "--max-turns",
        str(max_turns),
        "--model",
        model,
    ]
    if effort:
        cmd.extend(["--effort", effort])
    cmd.extend([
        "--yolo",
        "--tools-all",
        "--skip-onboarding",
        "--no-auto-update",
    ])
    return cmd


def checkpoint_path(project_dir: Path) -> Path:
    return project_dir / CHECKPOINT_FILE


def load_checkpoint(project_dir: Path) -> dict[str, int]:
    path = checkpoint_path(project_dir)
    try:
        with path.open(encoding="utf-8") as checkpoint:
            state = json.load(checkpoint)
    except FileNotFoundError as error:
        raise ValueError(f"no checkpoint found at {path}; start without --resume") from error
    except json.JSONDecodeError as error:
        raise ValueError(f"checkpoint at {path} is invalid") from error

    required = {"loops", "widgets_per_run", "max_turns", "current_loop", "current_index"}
    if not isinstance(state, dict) or not required <= state.keys():
        raise ValueError(f"checkpoint at {path} is incomplete")
    if not all(isinstance(state[key], int) for key in required):
        raise ValueError(f"checkpoint at {path} contains invalid values")
    return state


def save_checkpoint(project_dir: Path, args: argparse.Namespace, loop: int, index: int) -> None:
    """Atomically save the batch that must be run next."""
    path = checkpoint_path(project_dir)
    temporary_path = path.with_suffix(".tmp")
    state = {
        "loops": args.loops,
        "widgets_per_run": args.widgets_per_run,
        "max_turns": args.max_turns,
        "current_loop": loop,
        "current_index": index,
    }
    with temporary_path.open("w", encoding="utf-8") as checkpoint:
        json.dump(state, checkpoint, indent=2)
        checkpoint.write("\n")
        checkpoint.flush()
    temporary_path.replace(path)


def validate_checkpoint(state: dict[str, int], args: argparse.Namespace) -> None:
    expected = {
        "loops": args.loops,
        "widgets_per_run": args.widgets_per_run,
        "max_turns": args.max_turns,
    }
    mismatches = [key for key, value in expected.items() if state.get(key) != value]
    if mismatches:
        print(
            f"[CHECKPOINT INFO] Resuming with modified parameters (mismatches: {', '.join(mismatches)}). "
            f"Resuming execution from loop {state['current_loop']}, widget {state['current_index']}.",
            file=sys.stderr,
        )
    if not 1 <= state["current_loop"] <= args.loops:
        raise ValueError("checkpoint current_loop is out of range")
    if not 1 <= state["current_index"] <= TOTAL_WIDGETS:
        raise ValueError("checkpoint current_index is out of range")


def auto_commit_progress(project_dir: Path, pass_number: int, start: int, end: int, note: str = "") -> None:
    """Stage and commit modified source files and documentation to ensure work is never lost."""
    if not (project_dir / ".git").exists():
        return
    try:
        status = subprocess.run(
            ["git", "status", "--porcelain"],
            cwd=project_dir,
            capture_output=True,
            text=True,
            check=False,
        )
        if status.stdout.strip():
            subprocess.run(
                ["git", "add", "app", INVENTORY_FILE, "PROCEDURE*.md", "WIDGET*.md", "README*.md"],
                cwd=project_dir,
                check=False,
            )
            commit_msg = (
                f"chore(orchestrator): checkpoint widgets {start}-{end} "
                f"(loop {pass_number}){note}"
            )
            subprocess.run(
                ["git", "commit", "-m", commit_msg],
                cwd=project_dir,
                check=False,
            )
            print(f"[GIT SAFETY] Work saved: {commit_msg}", file=sys.stderr)
    except Exception as exc:
        print(f"[GIT SAFETY WARNING] Could not auto-commit changes: {exc}", file=sys.stderr)


def run_batch_with_retry(
    command: list[str],
    project_dir: Path,
    args: argparse.Namespace,
    pass_number: int,
    start: int,
    end: int,
) -> int:
    """Run a Command Code batch with exponential backoff and error safety."""
    attempts = 0
    max_attempts = max(1, args.max_retries + 1)

    while attempts < max_attempts:
        attempts += 1
        # Save checkpoint before execution so interruption or crash restarts at this exact batch
        save_checkpoint(project_dir, args, pass_number, start)

        result = subprocess.run(command, cwd=project_dir, check=False)
        returncode = result.returncode

        # Check for uncommitted work immediately after each run attempt to guarantee safety
        auto_commit_progress(project_dir, pass_number, start, end)

        if returncode == 0:
            return 0

        # CommandCode returns exit code 8 when conversation turns cap (--max-turns) is reached
        if returncode == 8:
            print(
                f"[WARNING] Command Code reached max turns ({args.max_turns}) for widgets {start}-{end}. "
                "Progress was saved.",
                file=sys.stderr,
            )
            return 0

        if attempts < max_attempts:
            delay = args.retry_delay * (2 ** (attempts - 1))
            print(
                f"[ERROR SAFETY] Batch {start}-{end} failed with exit code {returncode}. "
                f"Retrying in {delay:.1f}s (attempt {attempts}/{args.max_retries})...",
                file=sys.stderr,
            )
            try:
                time.sleep(delay)
            except KeyboardInterrupt:
                raise
        else:
            print(
                f"[ERROR SAFETY] Batch {start}-{end} failed after {max_attempts} attempts "
                f"(exit code {returncode}). Work checkpoint is safely preserved at widget {start}.",
                file=sys.stderr,
            )
            return returncode

    return 1


def main() -> int:
    args = parse_args()
    try:
        validate_args(args)
    except ValueError as error:
        print(f"error: {error}", file=sys.stderr)
        return 2

    project_dir = Path.cwd()
    ensure_widget_inventory(project_dir)
    target_apk = find_target_apk(project_dir)
    apk_name = target_apk.name if target_apk else "MyNothingWidgets.apkm"

    start_widget = args.start_widget
    end_widget = args.end_widget if args.end_widget is not None else TOTAL_WIDGETS
    total_in_scope = end_widget - start_widget + 1

    batches_per_pass = math.ceil(total_in_scope / args.widgets_per_run)
    total_runs = args.loops * batches_per_pass
    first_loop = 1
    first_index = start_widget

    if args.resume:
        try:
            state = load_checkpoint(project_dir)
            validate_checkpoint(state, args)
        except ValueError as error:
            print(f"error: {error}", file=sys.stderr)
            return 2
        first_loop = state["current_loop"]
        first_index = state["current_index"]
        print(
            f"Resuming at pass {first_loop}/{args.loops}, widget {first_index}.",
            file=sys.stderr,
        )

    print(
        f"Target APK: {apk_name} | Scope: widgets {start_widget}-{end_widget} ({total_in_scope} total)\n"
        f"{args.loops} pass(es) × {batches_per_pass} batch(es) = {total_runs} "
        f"Command Code run(s) in {project_dir}.",
        file=sys.stderr,
    )
    if not args.execute:
        print("Dry run only. Add --execute to run these commands.", file=sys.stderr)

    run_number = (first_loop - 1) * batches_per_pass + (first_index - start_widget) // args.widgets_per_run
    try:
        for pass_number in range(first_loop, args.loops + 1):
            initial_index = first_index if pass_number == first_loop else start_widget
            for start in range(initial_index, end_widget + 1, args.widgets_per_run):
                end = min(start + args.widgets_per_run - 1, end_widget)
                run_number += 1
                command = command_for(
                    start=start,
                    end=end,
                    max_turns=args.max_turns,
                    loop=pass_number,
                    model=args.model,
                    effort=args.effort,
                    target_apk=apk_name,
                )
                print(
                    f"\n# Run {run_number}/{total_runs}; pass {pass_number}/{args.loops}; "
                    f"widgets {start}-{end}"
                )
                print(shlex.join(command))

                if args.execute:
                    returncode = run_batch_with_retry(
                        command=command,
                        project_dir=project_dir,
                        args=args,
                        pass_number=pass_number,
                        start=start,
                        end=end,
                    )
                    if returncode != 0:
                        print(
                            f"Execution stopped at widgets {start}-{end}. "
                            f"Resume to retry it.",
                            file=sys.stderr,
                        )
                        return returncode

                    # Update checkpoint for next batch
                    if end == end_widget and pass_number == args.loops:
                        checkpoint_path(project_dir).unlink(missing_ok=True)
                        print("\n[COMPLETE] All widget import passes completed successfully!", file=sys.stderr)
                    elif end == end_widget:
                        save_checkpoint(project_dir, args, pass_number + 1, start_widget)
                    else:
                        save_checkpoint(project_dir, args, pass_number, end + 1)

                    # Inter-batch pacing delay to avoid hitting request / token rate limits
                    if args.delay_seconds > 0 and not (end == end_widget and pass_number == args.loops):
                        time.sleep(args.delay_seconds)

    except KeyboardInterrupt:
        auto_commit_progress(project_dir, first_loop, first_index, end_widget, note=" [interrupted]")
        effort_flag = f" --effort {args.effort}" if args.effort else ""
        print(
            f"\nInterrupted. Resume with: python3 orchestrator.py {args.loops} "
            f"{args.widgets_per_run} --max-turns {args.max_turns} --model {args.model}"
            f"{effort_flag} --execute --resume",
            file=sys.stderr,
        )
        return 130

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
