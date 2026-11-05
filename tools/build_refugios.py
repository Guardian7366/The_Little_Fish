"""Generate the updated map 5 from the original side-room map."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]/'src/main/resources/maps'
grid = [list(map(int,row.split())) for row in (root/'extraStage.txt').read_text().splitlines()]

def decorate(x0,y0,x1,y1,base,door,bottom_border=True):
    for y in range(y0,y1+1):
        for x in range(x0,x1+1):
            grid[y][x]=base+(x+y//2)%2
        grid[y][x0-1]=grid[y][x1+1]=base+2
    for x in range(x0-1,x1+2):
        if bottom_border:
            grid[y1+1][x]=base+2
        grid[y0-1][x]=base+3
    x,y=door
    grid[y][x]=86

decorate(9,35,22,44,107,(9,40))
decorate(27,35,40,44,113,(40,40))
decorate(27,24,40,33,119,(40,29))
for x,y in [(8,35),(23,44),(26,35),(41,44),(26,24),(41,33)]:
    grid[y][x]=0
for y in range(35,45):
    grid[y][18]=109
    grid[y][31]=115
grid[41][18]=111
grid[37][31]=117
for x in range(13,16):
    grid[38][x]=110
for y in range(38,43):
    grid[y][38]=116
for y in range(24,34):
    grid[y][36]=125
grid[26][36]=119
grid[31][36]=123
for x in range(28,36):
    grid[28][x]=125
for y in range(29,34):
    grid[y][30]=125
grid[30][30]=119
# Later refuges retain the original floor rectangles and reward identities.
decorate(9,24,22,33,126,(9,29),bottom_border=False)
decorate(9,14,22,22,132,(9,19),bottom_border=False)
decorate(27,14,40,22,138,(40,19),bottom_border=False)
for x,y in [(8,24),(23,33),(8,14),(23,22),(26,14),(41,22)]:
    grid[y][x]=0

# Pump. Both short routes can be approached from the entrance.
for y in range(24,34):
    grid[y][19]=128
grid[31][19]=130
for y in range(24,27):
    grid[y][14]=128
for y in range(28,30):
    for x in range(14,18):
        grid[y][x]=128
for y in range(31,34):
    grid[y][17]=128
grid[27][14]=144
grid[30][17]=144

# Hongui. The NPC canopy and reward alcove sit outside the arena.
for y in range(14,23):
    grid[y][19]=134
grid[21][19]=136
for y in range(14,20):
    grid[y][12]=134

# Ghost. The symbols remain freely accessible without any equipment.
for y in range(14,23):
    grid[y][31]=140
grid[21][31]=142
for x,y,tile in [(34,16,146),(37,16,148),(35,20,150)]:
    grid[y][x]=tile

# Handy's two sides meet at one covered crossing. The reward has its own gate.
decorate(27,5,40,12,152,(40,9),bottom_border=False)
for y in range(5,13):
    grid[y][30]=154
    grid[y][36]=154
grid[8][36]=152
grid[11][30]=156
grid[6][32]=164
grid[11][37]=164
grid[5][41]=0
grid[12][26]=0

# Ic's short tutorial leads to a hub, two turns and a wall stop.
decorate(9,5,22,12,158,(9,9),bottom_border=False)
for y in range(5,13):
    grid[y][19]=173
grid[10][19]=162
for x in range(12,19):
    grid[5][x]=173
for x,y in [(12,9),(13,9),(14,9),(16,9),(17,9),(18,9),(15,8),(15,7),
            (14,6),(12,6),(13,7),(15,10),(15,11),(16,12),(17,12),(18,11)]:
    grid[y][x]=166+(x+y)%2
for x,y in [(11,9),(15,9),(15,6),(15,12),(11,6),(18,8),(18,10)]:
    grid[y][x]=168
grid[6][13]=169
grid[12][18]=171
grid[5][8]=0
grid[12][23]=0

(root/'extraRefugios.txt').write_text(''.join(' '.join(map(str,row))+chr(10) for row in grid),encoding='utf-8')
