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
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.*;

public class MainActivity extends Activity {
    static final int CAM=1,GAL=2,PERMS=3;
    final int BG=Color.rgb(8,17,31), CARD=Color.rgb(17,31,51), WHITE=Color.WHITE, BLUE=Color.rgb(25,118,210);
    LinearLayout root;
    TextRecognizer ocr;
    String what="", date="", time="";

    public void onCreate(Bundle b){ super.onCreate(b); ocr=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS); askPerms(); home(); }

    void askPerms(){
        ArrayList<String> p=new ArrayList<>();
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.RECORD_AUDIO);
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.CAMERA);
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.POST_NOTIFICATIONS);
        if(!p.isEmpty())requestPermissions(p.toArray(new String[0]),PERMS);
    }

    TextView t(String s,int z,boolean b){ TextView v=new TextView(this);v.setText(s);v.setTextColor(WHITE);v.setTextSize(z);v.setPadding(10,10,10,10);if(b)v.setTypeface(null,1);return v; }
    Button b(String s){ Button v=new Button(this);v.setText(s);v.setTextColor(WHITE);v.setTextSize(17);v.setAllCaps(false);v.setBackgroundColor(BLUE);v.setPadding(10,20,10,20);return v; }
    LinearLayout card(){ LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(18,16,18,16);c.setBackgroundColor(CARD);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(8,10,8,10);c.setLayoutParams(p);return c; }
    void base(String sub){ ScrollView s=new ScrollView(this);s.setBackgroundColor(BG);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(14,30,14,30);s.addView(root);root.addView(t("Asistente Personal",25,true));root.addView(t(sub,14,false));setContentView(s); }

    void home(){
        base("Habla o elige una imagen. La app busca automáticamente qué, fecha y hora.");
        LinearLayout c=card();c.addView(t("Entrada rápida",19,true));
        Button v=b("🎙️  Hablar"), cam=b("📷  Hacer foto"), gal=b("🖼️  Galería");
        c.addView(v);c.addView(cam);c.addView(gal);root.addView(c);
        v.setOnClickListener(x->voice());cam.setOnClickListener(x->camera());gal.setOnClickListener(x->gallery());
    }

    void voice(){
        if(!SpeechRecognizer.isRecognitionAvailable(this)){toast("Reconocimiento de voz no disponible");return;}
        base("Habla con normalidad. Si dices fecha y hora, las pondré yo.");
        root.addView(t("🎙️ Te escucho…",24,true));
        SpeechRecognizer r=SpeechRecognizer.createSpeechRecognizer(this);
        r.setRecognitionListener(new RecognitionListener(){
            public void onReadyForSpeech(Bundle b){} public void onBeginningOfSpeech(){} public void onRmsChanged(float f){}
            public void onBufferReceived(byte[] b){} public void onEndOfSpeech(){} public void onPartialResults(Bundle b){}
            public void onEvent(int a,Bundle b){}
            public void onError(int e){toast("No lo he entendido. Inténtalo de nuevo.");home();}
            public void onResults(Bundle b){ArrayList<String> a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(a!=null&&!a.isEmpty())process(a.get(0));}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"es-ES");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);
        r.startListening(i);
    }

    void camera(){ Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);startActivityForResult(i,CAM); }
    void gallery(){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");startActivityForResult(i,GAL); }

    protected void onActivityResult(int req,int res,Intent data){
        super.onActivityResult(req,res,data);
        if(res!=RESULT_OK||data==null)return;
        if(req==CAM){
            Bundle ex=data.getExtras();
            Bitmap bm=ex==null?null:(Bitmap)ex.get("data");
            if(bm!=null)read(InputImage.fromBitmap(bm,0)); else toast("No pude leer la foto");
        }else if(req==GAL){
            Uri u=data.getData();
            if(u!=null)try{read(InputImage.fromFilePath(this,u));}catch(IOException e){toast("No pude abrir la imagen");}
        }
    }

    void read(InputImage i){
        base("Leyendo la imagen automáticamente…");
        root.addView(t("📷 Analizando…",22,true));
        ocr.process(i).addOnSuccessListener(x->{
            String s=x.getText();
            if(s==null||s.trim().isEmpty()){what="";date="";time="";verify();} else process(s);
        }).addOnFailureListener(x->{what="";date="";time="";verify();});
    }

    void process(String raw){
        String clean=raw.replace('\n',' ').replaceAll("\\s+"," ").trim();
        String l=clean.toLowerCase(new Locale("es","ES"));
        what=clean; date=""; time="";

        LocalDate today=LocalDate.now();

        if(l.contains("pasado mañana")) date=today.plusDays(2).toString();
        else if(l.contains("mañana")) date=today.plusDays(1).toString();
        else if(l.matches(".*\\bhoy\\b.*")) date=today.toString();

        Matcher d=Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?\\b").matcher(l);
        if(d.find()){
            int y=d.group(3)==null?today.getYear():Integer.parseInt(d.group(3));
            if(y<100)y+=2000;
            try{LocalDate parsed=LocalDate.of(y,Integer.parseInt(d.group(2)),Integer.parseInt(d.group(1)));if(d.group(3)==null&&parsed.isBefore(today))parsed=parsed.plusYears(1);date=parsed.toString();}catch(Exception ignored){}
        }

        if(date.isEmpty()){
            String months="enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|setiembre|octubre|noviembre|diciembre";
            Matcher md=Pattern.compile("\\b(\\d{1,2})\\s+(?:de\\s+)?("+months+")(?:\\s+(?:de\\s+)?(\\d{4}))?\\b").matcher(l);
            if(md.find()){
                int month=monthNumber(md.group(2));
                int year=md.group(3)==null?today.getYear():Integer.parseInt(md.group(3));
                try{LocalDate parsed=LocalDate.of(year,month,Integer.parseInt(md.group(1)));if(md.group(3)==null&&parsed.isBefore(today))parsed=parsed.plusYears(1);date=parsed.toString();}catch(Exception ignored){}
            }
        }

        if(date.isEmpty()){
            Map<String,DayOfWeek> days=new LinkedHashMap<>();
            days.put("lunes",DayOfWeek.MONDAY); days.put("martes",DayOfWeek.TUESDAY); days.put("miércoles",DayOfWeek.WEDNESDAY); days.put("miercoles",DayOfWeek.WEDNESDAY);
            days.put("jueves",DayOfWeek.THURSDAY); days.put("viernes",DayOfWeek.FRIDAY); days.put("sábado",DayOfWeek.SATURDAY); days.put("sabado",DayOfWeek.SATURDAY); days.put("domingo",DayOfWeek.SUNDAY);
            for(Map.Entry<String,DayOfWeek> e:days.entrySet()) if(l.matches(".*\\b"+e.getKey()+"\\b.*")){ date=today.with(TemporalAdjusters.nextOrSame(e.getValue())).toString(); break; }
        }

        Matcher tm=Pattern.compile("(?i)(?:a\\s+las?|a\\s+la|hora\\s*)?\\b([01]?\\d|2[0-3])[:.h]([0-5]\\d)\\b").matcher(l);
        if(tm.find()) time=String.format(Locale.US,"%02d:%02d",Integer.parseInt(tm.group(1)),Integer.parseInt(tm.group(2)));

        if(time.isEmpty()){
            Matcher th=Pattern.compile("(?i)(?:a\\s+las?|a\\s+la|sobre\\s+las?)\\s+([01]?\\d|2[0-3])(?:\\s*(?:h|horas?))?\\b").matcher(l);
            if(th.find()) time=String.format(Locale.US,"%02d:00",Integer.parseInt(th.group(1)));
        }

        if(time.isEmpty()){
            Matcher tw=Pattern.compile("(?i)(?:a\\s+las?|a\\s+la|sobre\\s+las?)\\s+(una|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce|trece|catorce|quince|dieciseis|dieciséis|diecisiete|dieciocho|diecinueve|veinte|veintiuna|veintiuno|veintidos|veintidós|veintitres|veintitrés)(?:\\s+y\\s+(cuarto|media|\\d{1,2}))?\\b").matcher(l);
            if(tw.find()){
                int h=wordHour(tw.group(1)), m=0;
                if(tw.group(2)!=null){ if(tw.group(2).equals("cuarto"))m=15; else if(tw.group(2).equals("media"))m=30; else m=Integer.parseInt(tw.group(2)); }
                if(h>=0) time=String.format(Locale.US,"%02d:%02d",h,m);
            }
        }

        verify();
    }

    int monthNumber(String m){
        String[] a={"enero","febrero","marzo","abril","mayo","junio","julio","agosto","septiembre","octubre","noviembre","diciembre"};
        if(m.equals("setiembre"))m="septiembre";
        for(int i=0;i<a.length;i++)if(a[i].equals(m))return i+1;
        return 0;
    }

    int wordHour(String s){
        s=s.replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u");
        String[] w={"","una","dos","tres","cuatro","cinco","seis","siete","ocho","nueve","diez","once","doce","trece","catorce","quince","dieciseis","diecisiete","dieciocho","diecinueve","veinte","veintiuna","veintidos","veintitres"};
        for(int i=1;i<w.length;i++)if(w[i].equals(s))return i;
        if(s.equals("veintiuno"))return 21;
        return -1;
    }

    void verify(){
        if(what.length()<3){
            EditText e=new EditText(this);
            new AlertDialog.Builder(this).setTitle("Falta un dato").setMessage("¿Qué tengo que recordar?").setView(e).setCancelable(false)
                .setPositiveButton("Confirmar",(d,w)->{what=e.getText().toString().trim();verify();}).show();return;
        }
        if(date.isEmpty()){
            LocalDate n=LocalDate.now();
            new DatePickerDialog(this,(v,y,m,d)->{date=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);verify();},n.getYear(),n.getMonthValue()-1,n.getDayOfMonth()).show();return;
        }
        if(time.isEmpty()){
            LocalTime n=LocalTime.now();
            new TimePickerDialog(this,(v,h,m)->{time=String.format(Locale.US,"%02d:%02d",h,m);verify();},n.getHour(),n.getMinute(),true).show();return;
        }
        ready();
    }

    void ready(){
        base("Fecha y hora recuperadas. Elige solo cuándo quieres el aviso.");
        LinearLayout c=card();
        c.addView(t(date+" · "+time,19,true));
        c.addView(t(what,22,true));
        c.addView(t("¿Cuándo quieres que te avise?",19,true));
        String[] s={"10 min antes","30 min antes","1 hora antes","1 día antes"};
        int[] m={10,30,60,1440};
        for(int k=0;k<s.length;k++){Button x=b(s[k]);final int mm=m[k];c.addView(x);x.setOnClickListener(v->schedule(mm));}
        Button custom=b("📅  Personalizar día y hora del aviso");
        c.addView(custom); custom.setOnClickListener(v->customReminder());
        root.addView(c);
    }

    void customReminder(){
        LocalDate eventDate;
        LocalTime eventTime;
        try{eventDate=LocalDate.parse(date);eventTime=LocalTime.parse(time);}catch(Exception e){toast("No pude leer la fecha de la cita");return;}
        final LocalDate ed=eventDate; final LocalTime et=eventTime;
        new DatePickerDialog(this,(dv,y,m,d)->{
            LocalDate chosen=LocalDate.of(y,m+1,d);
            new TimePickerDialog(this,(tv,h,min)->{
                LocalDateTime alert=LocalDateTime.of(chosen,LocalTime.of(h,min));
                scheduleAt(alert);
            },et.getHour(),et.getMinute(),true).show();
        },ed.getYear(),ed.getMonthValue()-1,ed.getDayOfMonth()).show();
    }

    void schedule(int before){
        try{
            LocalDateTime ev=LocalDateTime.parse(date+"T"+time);
            scheduleAt(ev.minusMinutes(before));
        }catch(Exception e){toast("No pude programar el aviso");}
    }

    void scheduleAt(LocalDateTime alert){
        try{
            LocalDateTime ev=LocalDateTime.parse(date+"T"+time);
            if(alert.isAfter(ev)){toast("El aviso no puede ser después de la cita");return;}
            if(alert.isBefore(LocalDateTime.now())){toast("Ese momento de aviso ya ha pasado. Elige otro.");return;}
            long when=alert.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            Intent i=new Intent(this,AlarmReceiver.class);i.putExtra("what",what);
            PendingIntent pi=PendingIntent.getBroadcast(this,(int)(System.currentTimeMillis()%Integer.MAX_VALUE),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
            toast("Recordatorio guardado para "+alert.toLocalDate()+" · "+alert.toLocalTime());
            home();
        }catch(Exception e){toast("No pude programar el aviso");}
    }

    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public void onBackPressed(){home();}
}
