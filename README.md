# AuroraWars
An official points-based war plugin for AuroraUniverse

![Preview Gif](/preview.gif)

### Commands
- /twar (aurorawars.use, aurorawars.mayor to declare) - open GUI to send war declare request 
- /twar declare <attacker> <victim> (aurorawars.admin) - declare a war without request
- /twar end <town> (aurorawars.admin) - end war without any punishment
- /twar fend <town> (aurorawars.admin) - end war with winner and loser
- /twar placeflag - places flag on enemy's ground, where you are staying
## How it works

1. Open wars menu on /twar
2. Select town to send war request or start raid
3. Left mouse button for war request and right to raid start

War can be two types: flag and points-based. 

With flag wars players can get enemy's regions annexed. When home region is annexed, the town wins.

Raids are just forcing pvp mode for both towns for specific amount of time with cooldown.

## Dependencies 
Plugin requires EPCAPI for work properly
https://github.com/karlovm/EasyPluginCore/tree/0.4
