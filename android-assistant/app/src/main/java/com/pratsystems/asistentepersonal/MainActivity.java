package com.pratsystems.asistentepersonal;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.speech.*;
import android.view.*;
import android.widget.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.regex.*;

public class MainActivity extends Activity {
    static final int CAM=1,GAL=2,PERMS=3;
    final int BG=Color.rgb(8,17,31), CARD=Color.rgb(17,31,51), WHITE=Color.WHITE, BLUE=Color.rgb(25,118,210);
    LinearLayout root;
    TextRecognizer ocr;
    String what="", date="", time="";

    public void onCreate(Bundle b){ super.onCreate(b); ocr=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS); askPerms(); home(); }
    void askPerms(){ ArrayList<String> p=new ArrayList<>(); if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.RECORD_AUDIO); if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.CAMERA); if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.POST_NOTIFICATIONS); if(!p.isEmpty())requestPermissions(p.toArray(new String[0]),PERMS); }
    TextView t(String s,int z,boolean b){ TextView v=new TextView(this);v.setText(s);v.setTextColor(WHITE);v.setTextSize(z);v.setPadding(10,10,10,10);if(b)v.setTypeface(null,1);return v; }
    Button b(String s){ Button v=new Button(this);v.setText(s);v.setTextColor(WHITE);v.setTextSize(17);v.setAllCaps(false);v.setBackgroundColor(BLUE);v.setPadding(10,20,10,20);return v; }
    LinearLayout card(){ LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(18,16,18,16);c.setBackgroundColor(CARD);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(8,10,8,10);c.setLayoutParams(p);return c; }
    void base(String sub){ ScrollView s=new ScrollView(this);s.setBackgroundColor(BG);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(14,30,14,30);s.addView(root);root.addView(t("Asistente Personal",25,true));root.addView(t(sub,14,false));setContentView(s); }
    void home(){ base("Habla o elige una imagen. La app prepara el recordatorio."); LinearLayout c=card();c.addView(t("Entrada rápida",19,true));Button v=b("🎙️  Hablar"), cam=b("📷  Hacer foto"), gal=b("🖼️  Galería");c.addView(v);c.addView(cam);c.addView(gal);root.addView(c);v.setOnClickListener(x->voice());cam.setOnClickListener(x->camera());gal.setOnClickListener(x->gallery()); }
    void voice(){ if(!SpeechRecognizer.isRecognitionAvailable(this)){toast("Reconocimiento de voz no disponible");return;} base("Habla con normalidad.");root.addView(t("🎙️ Te escucho…",24,true)); SpeechRecognizer r=SpeechRecognizer.createSpeechRecognizer(this);r.setRecognitionListener(new RecognitionListener(){public void onReadyForSpeech(Bundle b){}public void onBeginningOfSpeech(){}public void onRmsChanged(float f){}public void onBufferReceived(byte[] b){}public void onEndOfSpeech(){}public void onPartialResults(Bundle b){}public void onEvent(int a,Bundle b){}public void onError(int e){toast("No lo he entendido. Inténtalo de nuevo.");home();}public void onResults(Bundle b){ArrayList<String> a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(a!=null&&!a.isEmpty())process(a.get(0));}});Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"es-ES");i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);r.startListening(i); }
    void camera(){ Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);startActivityForResult(i,CAM); }
    void gallery(){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");startActivityForResult(i,GAL); }
    protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(res!=RESULT_OK||data==null)return;if(req==CAM){Bitmap bm=(Bitmap)data.getExtras().get("data");if(bm!=null)read(InputImage.fromBitmap(bm,0));}else if(req==GAL){Uri u=data.getData();if(u!=null)try{read(InputImage.fromFilePath(this,u));}catch(IOException e){toast("No pude abrir la imagen");}} }
    void read(InputImage i){ base("Leyendo la imagen automáticamente…");root.addView(t("📷 Analizando…",22,true));ocr.process(i).addOnSuccessListener(x->{String s=x.getText();if(s==null||s.trim().isEmpty()){what="";date="";time="";verify();}else process(s);}).addOnFailureListener(x->{what="";date="";time="";verify();}); }
    void process(String raw){ String l=raw.toLowerCase(Locale.ROOT); what=raw.replace('\n',' ').replaceAll("\\s+"," ").trim(); date="";time=""; if(l.contains("pasado mañana"))date=LocalDate.now().plusDays(2).toString();else if(l.contains("mañana"))date=LocalDate.now().plusDays(1).toString();else if(l.contains("hoy"))date=LocalDate.now().toString();Matcher d=Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?\\b").matcher(raw);if(d.find()){int y=d.group(3)==null?LocalDate.now().getYear():Integer.parseInt(d.group(3));if(y<100)y+=2000;try{date=LocalDate.of(y,Integer.parseInt(d.group(2)),Integer.parseInt(d.group(1))).toString();}catch(Exception ignored){}}Matcher tm=Pattern.compile("(?i)(?:a\\s+las?|hora\\s*)(\\d{1,2})(?:[:.](\\d{2}))?").matcher(raw);if(tm.find())time=String.format(Locale.US,"%02d:%02d",Integer.parseInt(tm.group(1)),tm.group(2)==null?0:Integer.parseInt(tm.group(2)));what=what.replaceAll("(?i)\\b(recu[eé]rdame|tengo que|hay que|debo|necesito|hoy|mañana|pasado mañana)\\b"," ").replaceAll("\\b\\d{1,2}[/-]\\d{1,2}(?:[/-]\\d{2,4})?\\b"," ").replaceAll("(?i)\\b(a\\s+las?|hora)\\s*\\d{1,2}(?:[:.]\\d{2})?\\b"," ").replaceAll("\\s+"," ").trim();verify(); }
    void verify(){ if(what.length()<3){ EditText e=new EditText(this);new AlertDialog.Builder(this).setTitle("Necesito verificar este dato").setMessage("¿Qué tengo que recordar?").setView(e).setCancelable(false).setPositiveButton("Confirmar",(d,w)->{what=e.getText().toString().trim();verify();}).show();return;}if(date.isEmpty()){LocalDate n=LocalDate.now();new DatePickerDialog(this,(v,y,m,d)->{date=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);verify();},n.getYear(),n.getMonthValue()-1,n.getDayOfMonth()).show();return;}if(time.isEmpty()){LocalTime n=LocalTime.now();new TimePickerDialog(this,(v,h,m)->{time=String.format(Locale.US,"%02d:%02d",h,m);verify();},n.getHour(),n.getMinute(),true).show();return;}ready(); }
    void ready(){ base("Listo. Solo falta decidir el aviso.");LinearLayout c=card();c.addView(t(date+" · "+time,19,true));c.addView(t(what,23,true));c.addView(t("¿Cuándo quieres que te avise?",19,true));String[] s={"30 min antes","1 hora antes","2 horas antes","12 horas antes","1 día antes"};int[] m={30,60,120,720,1440};for(int k=0;k<s.length;k++){Button x=b(s[k]);final int mm=m[k];c.addView(x);x.setOnClickListener(v->schedule(mm));}root.addView(c); }
    void schedule(int before){ try{LocalDateTime ev=LocalDateTime.parse(date+"T"+time);long when=ev.minusMinutes(before).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();Intent i=new Intent(this,AlarmReceiver.class);i.putExtra("what",what);PendingIntent pi=PendingIntent.getBroadcast(this,(int)(System.currentTimeMillis()%Integer.MAX_VALUE),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);toast("Recordatorio guardado");home();}catch(Exception e){toast("No pude programar el aviso");} }
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public void onBackPressed(){home();}
}
