"""Build the 50x50 purple abyss expansion, without rewriting existing maps."""
from pathlib import Path

grid = [[0] * 50 for _ in range(50)]

def rect(left, top, right, bottom, tile=69):
    for y in range(top, bottom + 1):
        for x in range(left, right + 1):
            grid[y][x] = tile

for room in [
    (21,40,29,45), (24,38,26,40), (19,33,31,38), # refuge and induction
    (19,31,21,34), (29,31,31,34),
    (15,25,22,32), (28,25,34,32), (15,22,34,25), (25,25,26,33), # fork and reunion
    (34,26,36,28), (36,24,43,30),               # optional workshop
    (24,18,25,22), (15,12,33,18),               # gate and mixed gallery
    (18,10,20,12), (30,10,32,12),
    (15,5,22,11), (27,5,34,11), (21,4,28,6), (24,3,26,4),
    (34,9,36,10), (36,8,41,12),                 # final side chamber
]:
    rect(*room)
floor = {(x,y) for y in range(50) for x in range(50) if grid[y][x] == 69}
for x,y in floor:
    for dx in (-1,0,1):
        for dy in (-1,0,1):
            if grid[y+dy][x+dx] == 0:
                grid[y+dy][x+dx] = 88
for pillar in [(38,26,38,27), (22,16,23,16), (30,30,30,30)]:
    rect(*pillar, 88)

walls = [(0,-1), (0,1), (-1,0), (1,0)]
for x,y in sorted(floor):
    if grid[y][x] == 69 and (x*3+y*5)%13 < 3:
        for (dx,dy), base in zip(walls, (70,73,76,78)):
            if grid[y+dy][x+dx] == 88:
                grid[y][x] = base + (x+y)%2
                break

for x,y in [(22,42),(23,38),(25,34),(30,35),(16,29),(33,29),(40,28),
            (24,21),(25,19),(27,12),(16,13),(19,7),(31,7),(25,5),(40,10)]:
    for direction,(dx,dy) in enumerate(walls):
        if grid[y+dy][x+dx] == 88:
            break
    else:
        direction = 0
        grid[y-1][x] = 88
    grid[y][x] = 89 + direction

for x,y,tile in [(25,41,16),(23,45,68),(25,3,68)]:
    grid[y][x] = tile
Path('src/main/resources/maps/sixthAbyss.txt').write_text(
    '\n'.join(' '.join(map(str,row)) for row in grid)+'\n', encoding='utf-8')
