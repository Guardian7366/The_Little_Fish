from pathlib import Path
grid=[[0]*50 for _ in range(50)]
def rect(a,b,c,d,t=2):
 for y in range(b,d+1):
  for x in range(a,c+1):grid[y][x]=t
rooms=[(16,5,29,9),(26,9,37,16),(18,14,25,18),(24,15,27,17),(22,19,23,23),(9,22,16,33),(17,23,29,29),(33,21,40,34),(29,24,33,26),(26,30,28,34),(10,34,25,43),(24,35,38,43),(19,42,22,44),(9,17,11,22),(37,13,40,16),(15,32,19,36)]
for r in rooms:rect(*r)
floor={(x,y) for y in range(50) for x in range(50) if grid[y][x]==2}
for x,y in floor:
 for dx in [-1,0,1]:
  for dy in [-1,0,1]:
   if grid[y+dy][x+dx]==0:grid[y+dy][x+dx]=3
for r in [(32,10,32,12),(14,24,14,25),(34,26,35,27),(29,37,30,38),(14,40,15,40),(33,40,33,41)]:rect(*r,3)
# Optional rock shortcut joins the central chamber to the lower circuit.
rect(26,30,26,31,3);rect(28,30,28,31,3)
rect(16,33,18,35,5);rect(18,15,20,17,5)
for x,y,t in [(22,7,18),(17,6,16),(28,6,16),(39,13,86),(9,17,86),(19,16,86),(20,44,18)]:grid[y][x]=t
for x,y in [(26,10),(37,15),(10,24),(16,29),(34,19),(39,32),(11,36),(21,42),(36,38)]:
 if grid[y][x]==2:grid[y][x]=12
# Third-zone original navy floor and matching wall-attached coral family.
for y in range(50):
 for x in range(50):
  grid[y][x]={2:15,12:15,3:14,18:68}.get(grid[y][x],grid[y][x])
for y in range(1,49):
 for x in range(1,49):
  if grid[y][x]!=15:continue
  # Short clusters separated by bare wall; all decorations remain walkable.
  if (x*3+y*5)%11>=6:continue
  if grid[y-1][x]==14:grid[y][x]=62 if x%4 else 64
  elif grid[y+1][x]==14:grid[y][x]=63 if x%4 else 65
  elif grid[y][x-1]==14:grid[y][x]=67 if y%3 else 60
  elif grid[y][x+1]==14:grid[y][x]=59 if y%3 else 61
Path('src/main/resources/maps/stage3.txt').write_text('\n'.join(' '.join(map(str,row)) for row in grid)+'\n')
