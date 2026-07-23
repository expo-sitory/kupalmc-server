# CoordsSync

A Minecraft server plugin that saves coordinates in sync across multiple servers. 

## What's it do?

- **Saves your location** after leaving the server
- **Teleports you back** to exactly where you were when you join a different server
- **Stores everything** in MySQL as fallback storage
- **Uses Redis** to sync data fast for server transfer

## Permissions

- `coordssync.admin` - Admin commands (for reloading)
- `coordssync.use` - Let players get teleported to their saved coords

