"""Generate the updated map 5 from the original side-room map."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]/'src/main/resources/maps'
grid = [list(map(int,row.split())) for row in (root/'extraStage.txt').read_text().splitlines()]

def decorate(x0,y0,x1,y1,base,door):
    for y in range(y0,y1+1):
        for x in range(x0,x1+1):
            grid[y][x]=base+(x+y//2)%2
        grid[y][x0-1]=grid[y][x1+1]=base+2
    for x in range(x0-1,x1+2):
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
(root/'extraRefugios.txt').write_text(''.join(' '.join(map(str,row))+chr(10) for row in grid),encoding='utf-8')
