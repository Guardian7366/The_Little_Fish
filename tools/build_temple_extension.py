"""Generate the 50 by 50 temple extension from its chamber footprints."""
from pathlib import Path

grid = [[0]*50 for _ in range(50)]
rooms = (
    (19,40,29,45), (21,34,28,39), (23,29,26,34),  # refuge, vestibule
    (8,37,16,41), (16,40,19,42),  # west lower puzzle
    (13,29,20,35), (18,34,21,36), (10,20,17,26), (13,26,15,29),
    (17,21,20,23), (20,22,28,29), (23,17,26,22),  # west, crossing
    (13,10,20,17), (18,17,23,19), (6,9,10,16), (10,15,13,17),
    (33,30,41,38), (28,37,33,39),  # east puzzle
    (31,22,39,28), (28,26,31,28),
    (30,13,37,19), (26,17,30,19), (39,10,46,16), (37,14,39,16),
    (30,7,37,10), (31,10,33,13),  # north-east chambers
    (21,8,27,17), (24,5,24,8), (22,3,26,5),  # empty approach and gate
    (7,22,10,24), (40,24,44,26), (27,9,29,11),  # statue detours
)
for left, top, right, bottom in rooms:
    for y in range(top, bottom+1):
        for x in range(left, right+1):
            grid[y][x] = 97+(x*5+y*3)%3

# Solid rims frame the open-water void without changing the walkable footprint.
floor = [(x,y) for y in range(1,49) for x in range(1,49) if grid[y][x]]
for x,y in floor:
    for dy in (-1,0,1):
        for dx in (-1,0,1):
            if grid[y+dy][x+dx]==0:
                grid[y+dy][x+dx]=93 if dy>=0 else 94

for left,top in ((7,19),(43,21),(28,6)):  # statue backing
    for y in range(top,top+3):
        for x in range(left,left+2):
            if grid[y][x]==0:
                grid[y][x]=94

for x,y in [(20,41),(28,41),(20,35),(21,33),(17,29),(16,27),(9,21),
            (7,21),(43,23),(42,25),(29,8),(21,11),(23,7),(26,11),
            (6,10),(7,15),(10,38),(14,38),(37,30),(39,35),(30,23),
            (18,12),(22,24),(29,22),(34,12),(38,15),(32,6),(13,32)]:
    grid[y][x]=96
for x,y in [(20,24),(25,25),(19,13),(32,26),(35,23),(23,36),(26,36),
            (16,20),(45,14),(35,9),(29,10)]:
    grid[y][x]=95

for x in range(9,15):
    grid[39][x]=101
for y in range(10,16):
    grid[y][8]=102
for x in range(34,39):
    grid[32][x]=101
for y in range(33,37):
    grid[y][38]=102
grid[41][22]=103
grid[45][24]=104

target=Path(__file__).resolve().parents[1]/'src/main/resources/maps/templeExtension.txt'
target.write_text(''.join(' '.join(map(str,row))+'\n' for row in grid),encoding='utf-8')
