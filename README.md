# G-Trigger
G-Earth extension, 
Setup triggers on which a reaction will happen.

Trigger options:
- Key pressed
- Packet send to server
- Packet send to client
- You say command
- Anyone says command

Reaction options:
- Send packet to server
- Send packet to client

## Requested features
- [ ] Always on top toggle
- [x] Save/Load configurations
- [ ] Global on/off hotkey switch
- [x] Variable use in packets and commands for example:
  - `{in:Chat}{i:$(userId)}{s:"$(message)"}{i:$(gesture)}{i:$(style)}{i:0}{i:$(trackingId)}` to intercept any incoming chatpacket regardless of content
  - `{out:Chat}{s:"So you said $(message)"}{i:0}{i:0}` use the variables from the trigger in the reaction
