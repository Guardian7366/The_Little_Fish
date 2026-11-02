"""Author the tile map; no runtime dependency. Run from the project directory."""
from pathlib import Path

grid = [[0] * 50 for _ in range(50)]

def rect(x1, y1, x2, y2, tile=2):
    for y in range(y1, y2 + 1):
        for x in range(x1, x2 + 1):
            grid[y][x] = tile

# Rooms and generously sized links; all coordinates are (column, row).
for room in [(10,28,16,34), (17,30,18,32), (19,28,25,34),
             (21,25,23,27), (19,20,25,24), (26,21,29,23),
             (30,19,36,24), (34,25,36,28), (31,27,38,33),
             (37,24,39,26), (10,19,15,24), (12,25,14,27),
             (16,21,18,23), (27,18,29,20), (22,35,23,37),
             (20,37,25,39)]:
    rect(*room)

# Reef borders surround every traversable tile; dark water beyond is impassable.
floor = {(x,y) for y in range(50) for x in range(50) if grid[y][x] == 2}
for x,y in floor:
    for dx in (-1,0,1):
        for dy in (-1,0,1):
            if grid[y+dy][x+dx] == 0:
                grid[y+dy][x+dx] = 3

# Gentle shore, original landmarks, understated directional tiles.
rect(11,30,15,33,5)
for x,y,t in [(13,31,16),(17,31,52),(22,26,51),(26,22,52),
              (38,25,18),(28,18,86),(12,18,35),(23,40,39),
              (10,19,12),(15,19,13),(10,23,11),(15,24,55),
              (11,28,12),(16,28,13),(10,33,11),(16,34,54),
              (19,28,12),(25,28,13),(19,34,11),(25,34,11),
              (19,20,12),(25,20,13),(19,24,55),(25,24,54),
              (27,19,11),(29,19,11),(30,19,12),(36,19,13),
              (30,24,55),(36,24,54),(31,27,12),(38,27,13),
              (31,33,11),(38,33,54),(22,35,11),(23,37,11),
              (20,39,11),(25,39,11),(21,39,5),(22,39,5),
              (23,39,5),(24,39,5),(12,19,5),(13,19,5)]:
    grid[y][x] = t

Path('src/main/resources/maps/stage1.txt').write_text(
    '\n'.join(' '.join(map(str,row)) for row in grid) + '\n', encoding='utf-8')
