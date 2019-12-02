# Distributed chat and concurrent Game of Life

This repository contains two exercises:

## Distributed chat

Implementing a decentralized chat, except for a register to keep track of participants.
Each chat has the following qualities: the messages sent must be displayed in the same order by all users.
The chat supports dynamic set of participants. Finally, A user can request to be the only one to write in the chat for a limited time

To ensure causal ordering among peers, I implementated the algorithm presented in M. Naimi and O. Thiare, "Distributed Mutual Exclusion Based on Causal Ordering", Journal of Computer Science 5.

## Concurrent Game of Life

Implementing an actor based version of "The Game of Life". The key requirements are:
- Maximize throughput
- Maximize GUI responsiveness
- Visualizing each game status
