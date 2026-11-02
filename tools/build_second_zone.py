from pathlib import Path
grid=[[0]*50 for _ in range(50)]
def rect(a,b,c,d,t=2):
 for y in range(b,d+1):
  for x in range(a,c+1):grid[y][x]=t
rooms=[(8,39,16,44),(17,40,19,42),(19,38,33,44),(8,28,17,35),(10,36,12,39),(18,33,25,35),(19,23,31,30),(23,31,25,33),(32,24,34,26),(34,20,40,26),(23,17,25,23),(19,8,38,16),(37,17,39,20),(36,6,40,9),(8,26,10,28),(25,7,27,8)]
for r in rooms:rect(*r)
# Return loop from the first encounter to Tuti.
rect(29,31,31,38);rect(25,33,29,35)
floor={(x,y) for y in range(50) for x in range(50) if grid[y][x]==2}
for x,y in floor:
 for dx in [-1,0,1]:
  for dy in [-1,0,1]:
   if grid[y+dy][x+dx]==0:grid[y+dy][x+dx]=3
# Solid islands allow breaking each sentry's line of fire.
for r in [(27,40,28,41),(13,29,13,30),(23,24,23,25),(27,28,27,29),(37,21,37,22),(26,11,27,13),(33,12,34,13)]:rect(*r,3)
rect(9,40,15,43,5);rect(22,33,24,35,5)
# Decoration uses the original passable coral tiles at room edges.
for a,b,c,d in rooms:
 for x,y,t in [(a,b,12),(c,b,13),(a,d,11),(c,d,54)]:
  if grid[y][x]==2:grid[y][x]=t
for x,y,t in [(9,41,18),(14,41,16),(18,41,52),(9,34,86),(39,23,86),(39,7,18),(23,32,51),(24,19,51),(35,8,52),(8,25,39),(8,26,5),(26,6,35),(26,7,5),(10,27,11),(25,8,11),(28,16,55),(32,9,12)]:grid[y][x]=t
Path('src/main/resources/maps/stage2.txt').write_text('\n'.join(' '.join(map(str,row)) for row in grid)+'\n',encoding='utf-8')
