from pathlib import Path
grid=[[0]*50 for _ in range(50)]
def rect(a,b,c,d,t=15):
 for y in range(b,d+1):
  for x in range(a,c+1):grid[y][x]=t
# Quiet arrival, first gate, encounter, two arms and a shared lower circuit.
rooms=[(22,5,29,9),(25,10,26,12),(21,13,29,20),(12,18,21,20),(29,18,39,20),
 (10,21,15,28),(35,21,40,28),(12,29,14,33),(36,29,38,33),
 (14,31,22,34),(30,31,40,34),(22,32,30,34),(25,21,26,31),
 (27,28,29,30),(8,27,10,29),(8,30,9,31),(6,32,10,37),
 (25,35,26,37),(16,38,35,43),(24,44,27,45)]
for r in rooms:rect(*r)
floor={(x,y) for y in range(50) for x in range(50) if grid[y][x]==15}
for x,y in floor:
 for dx in [-1,0,1]:
  for dy in [-1,0,1]:
   if grid[y+dy][x+dx]==0:grid[y+dy][x+dx]=14
# Original matching wall coral family, spaced in short groups.
for y in range(1,49):
 for x in range(1,49):
  if grid[y][x]!=15 or (x*3+y*5)%11>=6:continue
  if grid[y-1][x]==14:grid[y][x]=62 if x%4 else 64
  elif grid[y+1][x]==14:grid[y][x]=63 if x%4 else 65
  elif grid[y][x-1]==14:grid[y][x]=67 if y%3 else 60
  elif grid[y][x+1]==14:grid[y][x]=59 if y%3 else 61
rect(23,6,28,8,5)
for x,y,t in [(25,8,16),(25,45,68),(7,37,5),(29,29,5),(40,33,5),(7,36,35),(29,28,35),(40,32,35)]:grid[y][x]=t
Path('src/main/resources/maps/fourthDepths.txt').write_text('\n'.join(' '.join(map(str,row)) for row in grid)+'\n')
