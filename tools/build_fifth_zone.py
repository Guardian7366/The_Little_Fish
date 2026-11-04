"""Reproduce only map 3. Prepared PNGs and all other map files stay untouched."""
from pathlib import Path

grid = [[0] * 50 for _ in range(50)]
def rect(x1, y1, x2, y2, tile=69):
    for y in range(y1, y2 + 1):
        for x in range(x1, x2 + 1):
            grid[y][x] = tile

rooms = [
    (21,18,29,25),                         # A: quiet refuge
    (29,21,33,24), (32,17,43,27),           # B: first readable ambush
    (32,12,35,18), (24,6,35,14),            # northern arm
    (9,8,24,11), (8,4,10,20),              # illuminated Iron approach and door
    (10,16,19,24), (17,19,22,22),           # D: quiet plaza and western approach
    (5,25,14,34), (10,23,13,26),            # western ambush / optional branch
    (13,31,16,34), (14,32,38,36),           # E: lower circuit
    (28,25,30,33), (31,27,33,33),           # main progression, alternate eastern descent
    (19,26,23,27), (22,24,24,27), (17,23,19,27), # Iron shortcut to refuge
    (14,4,15,7),                           # first metal practice alcove, no bypass
    (24,36,27,39), (18,38,32,44),           # F: staggered pair
    (9,43,18,44), (12,36,15,41),            # two-wide exit neck; no route around the metal gate
    (37,34,40,35), (39,36,40,39),           # optional Rocky pocket
]
for room in rooms:
    rect(*room)
floor = {(x,y) for y in range(1,49) for x in range(1,49) if grid[y][x] == 69}
for x,y in floor:
    for dx in (-1,0,1):
        for dy in (-1,0,1):
            if grid[y+dy][x+dx] == 0:
                grid[y+dy][x+dx] = 88
# Original purple decorations, only on passable floor adjacent to the new wall.
for x,y in sorted(floor):
    if (x*3+y*5)%13 < 3:
        if grid[y-1][x] == 88: grid[y][x] = 70 + x%3
        elif grid[y+1][x] == 88: grid[y][x] = 73 + x%3
        elif grid[y][x-1] == 88: grid[y][x] = 76 + y%2
        elif grid[y][x+1] == 88: grid[y][x] = 78 + y%2
lights = [(25,19),(34,18),(41,26),(30,7),(11,7),(16,17),
          (7,26),(15,33),(29,32),(21,39),(30,43),(15,44)]
for x,y in lights:
    # The wall name describes the attachment direction. Add a small wall pedestal
    # where the chamber has no adjacent wall; never put collision in the coral cell.
    if grid[y-1][x] == 88: direction=0
    elif grid[y+1][x] == 88: direction=1
    elif grid[y][x-1] == 88: direction=2
    elif grid[y][x+1] == 88: direction=3
    else:
        grid[y-1][x] = 88
        direction=0
    grid[y][x] = 89+direction
# Existing event locations remain stable. Door signs use the original entrance tile.
for x,y,t in [(25,20,16),(25,23,69),(9,5,86),(14,40,86),(9,44,68)]:
    grid[y][x]=t
grid[4][14]=35  # Optional mark beyond the first Iron practice wall.
Path('src/main/resources/maps/stage4.txt').write_text(
    '\n'.join(' '.join(map(str,row)) for row in grid)+'\n', encoding='utf-8')
