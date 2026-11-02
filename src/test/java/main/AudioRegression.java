package main;
import java.lang.reflect.Proxy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.sound.sampled.Clip;

public final class AudioRegression {
 static int passed;
 static void check(boolean value,String message) {if(!value)throw new AssertionError(message);passed++;System.out.println("PASS "+message);}
 static Clip fake(AtomicInteger starts, AtomicInteger closes, AtomicInteger active, AtomicInteger peak, AtomicBoolean wrongThread) {
  AtomicBoolean running=new AtomicBoolean();
  return (Clip)Proxy.newProxyInstance(Clip.class.getClassLoader(),new Class[]{Clip.class},(p,m,a)->{
   if(!Thread.currentThread().getName().startsWith("little-fish-audio-"))wrongThread.set(true);
   switch(m.getName()) {
    case "isOpen":return true;
    case "isRunning":return running.get();
    case "isControlSupported":return false;
    case "start":case "loop": starts.incrementAndGet();if(!running.getAndSet(true))peak.accumulateAndGet(active.incrementAndGet(),Math::max);return null;
    case "stop":if(running.getAndSet(false))active.decrementAndGet();return null;
    case "close":closes.incrementAndGet();return null;
   }
   return null;
  });
 }
 public static void main(String[] args)throws Exception {
  CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
  AtomicInteger opens=new AtomicInteger(),starts=new AtomicInteger(),closes=new AtomicInteger(),active=new AtomicInteger(),peak=new AtomicInteger();
  AtomicBoolean wrong=new AtomicBoolean();
  Sound audio=new Sound(8,index->{opens.incrementAndGet();if(index==21){entered.countDown();release.await();}return fake(starts,closes,active,peak,wrong);});
  try {
   audio.requestPlay(21,false);check(entered.await(2,TimeUnit.SECONDS),"Slow device operation entered");
   long start=System.nanoTime();
   for(int i=0;i<10000;i++)audio.requestPlay(21,false);
   audio.stop();audio.checkVolume();
   check((System.nanoTime()-start)/1e6<250,"Ten thousand input sounds and stop never wait for blocked device");
   Thread.sleep(2100); // Simulate the reproduced two-second driver wait.
   release.countDown();check(audio.awaitIdle(3000),"Worker recovers from device wait");
   check(starts.get()==0,"Stopped/stale sound requests are not replayed later");
   for(int i=0;i<30;i++){audio.requestPlay(21,false);check(audio.awaitIdle(2000),"Dialogue effect completes");}
   check(opens.get()==1 && closes.get()==0,"Repeated dialogue letters reuse one clip without closing");
   for(int i=0;i<12;i++){audio.requestPlay(i,false);check(audio.awaitIdle(2000),"Effect completes");}
   check(peak.get()<=8,"At most eight effects play simultaneously");
   check(!wrong.get(),"All audio operations stay off the caller thread");
  }finally {release.countDown();audio.close();check(audio.awaitClosed(5000),"Worker shuts down");}
  check(opens.get()==closes.get(),"Every cached clip closes exactly once at shutdown");
  AtomicInteger musicStarts=new AtomicInteger();
  Sound music=new Sound(1,i->fake(musicStarts,new AtomicInteger(),new AtomicInteger(),new AtomicInteger(),new AtomicBoolean()));
  music.requestPlay(6,true);check(music.awaitIdle(2000),"Looping music starts");music.stop();music.requestPlay(22,true);check(music.awaitIdle(2000),"Music can change after stop");
  music.close();check(music.awaitClosed(2000),"Music worker closes");
  check(musicStarts.get()==2,"Both music selections were played");
  System.out.println("AUDIO_REGRESSION PASS "+passed);
 }
}
