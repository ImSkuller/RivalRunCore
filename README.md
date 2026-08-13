# RivalRun

A Minecraft plugin for team-based speedrunning with friends.

Built for **Paper 26.2** (Minecraft 26.2, Java 25).

---

## About

RivalRun is a small **hobby project** I made because I wanted to play a **teams speedrunning game with my friends**.

This plugin is mainly for fun and learning, so it isn’t designed to be perfect or highly optimized.

---

## Features

* Team system with a GUI team selector (colored icons, leave/random-join buttons)
* Grace period + countdown before a run starts
* Real elapsed run timer, shown live on the sidebar and tab list
* Sidebar scoreboard: game state, timer, your team, alive/total per team
* Tab list header/footer, fully templated in config
* Server MOTD reflects the current game state (waiting/starting/running/post)
* Spectator system: dying mid-run makes you a spectator instead of respawning,
  with a compass/menu to teleport to remaining players
* Elimination win condition (last team standing) alongside the classic
  Ender Dragon kill win
* Win celebration: titles, sound, fireworks
* Admin control menu (`/rivalrun` with no args) alongside all the subcommands
* Non-destructive config migration - upgrading the plugin adds new config
  keys without touching anything you've already customized
* Friendly fire toggle, team locking, team chat formatting

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

`/rrdebug` has a handful of dev/testing commands, gated behind `commands.debug` in the config and requiring OP.

---

## Permissions

All `rivalrun.*` permission nodes are declared in `plugin.yml` (default `op`, except team select/leave/switch/spectate which default to everyone).

---

## Disclaimer

* The plugin is **not optimized**
* The code may be **messy or inconsistent**
* I am a **Java beginner**, and I started learning Java while building this project

---

## Installation

1. Download the latest `.jar` file
2. Place it in your server's `plugins/` folder
3. Start or restart your server (requires Paper 26.2+ and Java 25+)

---

## Usage

Basic flow:

1. Players pick a team from the GUI (or `/rr select`)
2. An admin starts the game (`/rr start` or the admin menu)
3. Play through the run - dying mid-run makes you a spectator
4. First team to kill the Ender Dragon (or the last team standing) wins

---

## License / Permissions

You are free to do anything with this plugin:

* Modify it
* Redistribute it
* Fork it as many times as you want
* Improve or optimize it
* Use parts of it in your own projects

No restrictions do whatever you want.

---

## Contributing

If you want to improve the plugin:

1. Fork the repository
2. Make your changes
3. Open a Pull Request

Or just keep your own version that’s completely fine.

---

## Notes

* This project exists mainly for **fun and learning**
* The code may not follow best practices
* Feel free to clean it up or expand on it

---

## Final Note

If you end up using or improving this plugin, that’s great. Have fun speedrunning.
