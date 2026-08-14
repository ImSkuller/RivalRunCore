# RivalRun

A Minecraft plugin for team-based speedrunning with friends.

Built for **Paper 26.2** (Minecraft 26.2, requires Java 25).

---

## About

RivalRun is a small **hobby project** I made because I wanted to play a **teams speedrunning game with my friends**. It isn't designed to be perfect or highly optimized - it's built for fun.

---

## Features

**Core gameplay**
* Team-based speedrunning: first team to kill the Ender Dragon wins, or last team standing (elimination win)
* Grace period + countdown before a run starts, with a real elapsed run timer
* Win celebration: personalized titles, sounds, fireworks
* Six speedrun achievements (Nether, blaze rod, ender pearl, eye of ender, the End, first hit on the dragon) - first team to reach each gets credit in chat and on the scoreboard

**Teams**
* GUI team selector with colored icons and leave/random-join buttons
* In-game custom team creator (name + color picker), or use the built-in presets
* Team-colored nametag, tab list, and chat formatting
* Friendly fire toggle, team locking

**Player balancing**
* Per-player buff/debuff system - health, hunger drain rate, speed, PvP damage dealt/taken, and on-kill rewards - set per player through a GUI before a game, locked once it starts

**Spectator**
* Teamless players automatically become spectators when a game starts
* Compass/menu to teleport to remaining players
* Admin can force any player into or out of spectator mode

**Live UI**
* Sidebar scoreboard, tab list header/footer, and a dynamic server MOTD, all reflecting the current game state

**Admin tools**
* Admin control menu (`/rivalrun` with no args)
* In-game settings editor - every config value is editable through GUIs, no file editing required
* Full world reset: regenerate the Overworld/Nether/End with a new random seed (confirm/cancel GUI)
* Non-destructive config migration - upgrades add new options without touching anything you've already customized

---

## Commands

`/rivalrun` (alias `/rr`) with no arguments opens the admin menu for admins.

| Subcommand | Description |
|---|---|
| `start` | Start the game |
| `pause` / `resume` | Pause or resume the current game |
| `end` | Force end the game with no winner |
| `reset` | Reset teams and game state |
| `select` / `switch` | Open the team select menu |
| `leave` | Leave your current team |
| `lock` / `unlock` | Lock/unlock team joining and switching |
| `spectate` | Open the spectator teleport menu (while spectating) |
| `spectator <player>` | Force a player into/out of spectator mode |
| `buffs` | Set per-player buffs/debuffs before a game starts |
| `resetworld` | Regenerate the entire world with a new seed (confirm/cancel GUI) |

`/rrdebug` has a handful of dev/testing commands, gated behind `commands.debug` in the config and requiring OP.

---

## Permissions

All `rivalrun.*` permission nodes are declared in `plugin.yml` (default `op`, except team select/leave/switch/spectate which default to everyone).

---

## Installation

1. Download the latest `.jar` file
2. Place it in your server's `plugins/` folder
3. Start or restart your server (requires Paper 26.2+ and Java 25+)

---

## Usage

1. Players pick a team from the GUI (or `/rr select`)
2. An admin optionally sets per-player buffs (`/rr buffs`) and adjusts settings (`/rr` admin menu)
3. An admin starts the game (`/rr start`)
4. Play through the run - teamless players spectate automatically
5. First team to kill the Ender Dragon (or the last team standing) wins

---

## License

Free to use however you want: modify it, redistribute it, fork it, use parts of it in your own projects. No restrictions.

## Contributing

Fork the repo, make your changes, open a Pull Request - or just keep your own version, that's fine too.

---

## Disclaimer

The plugin isn't optimized and the code may be messy - I'm a Java beginner and started learning Java while building this. Have fun speedrunning.
