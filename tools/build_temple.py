from pathlib import Path

grid = [[0]*50 for _ in range(50)]
def rect(a,b,c,d,tile=97):
    for y in range(b,d+1):
        for x in range(a,c+1):
            grid[y][x]=tile

for room in [
    (19,37,30,45), (22,29,27,37),                 # landing and lone Planta
    (17,27,33,34), (15,24,34,28), (17,17,34,24),   # lower loop and atrium
    (16,10,22,18), (22,7,27,18), (28,13,34,20),   # upper branches
    (24,5,24,7), (22,3,26,4),                    # one-tile gate and exit
    (10,9,12,17), (12,16,18,17),                  # NW gallery
    (33,11,41,13), (32,13,33,14),                 # NE gallery
    (7,30,15,32), (15,31,18,32),                  # SW gallery
    (35,28,37,36), (33,28,35,29),                 # SE gallery
    (8,20,17,22), (2,19,6,23),                   # west niche
    (34,22,41,24), (43,21,47,25),                # east niche
]:
    rect(*room)
for x,y in [(x,y) for y in range(1,49) for x in range(1,49) if grid[y][x]==97]:
    for dx in (-1,0,1):
        for dy in (-1,0,1):
            if grid[y+dy][x+dx]==0: grid[y+dy][x+dx]=93 if (x+y)%3 else 94
for y in range(1,49):
    for x in range(1,49):
        if grid[y][x]==97:
            grid[y][x]=97+(x*5+y*3)%3
rect(11,10,11,15,102)
rect(35,12,40,12,101)
rect(8,31,13,31,101)
rect(36,30,36,35,102)
for x,y,t in [(24,44,104),(24,30,103),(25,30,103),(21,41,103),
              (7,21,105),(42,23,106),(10,21,95),(40,22,95),(15,24,95),(34,24,95)]:
    grid[y][x]=t
for x,y in [(22,35),(23,8),(26,26)]:
    rect(x,y,x+2,y+1,94)
for x,y in [(20,40),(26,39),(23,37),(22,34),(28,32),(18,25),(34,25),
            (16,22),(28,18),(24,9),(10,12),(34,11),(10,30),(37,32),
            (5,19),(44,21),(22,7)]:
    grid[y][x]=96
Path('src/main/resources/maps/temple.txt').write_text(
    '\n'.join(' '.join(map(str,r)) for r in grid)+'\n',encoding='utf-8')
