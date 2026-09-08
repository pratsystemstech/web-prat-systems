package com.pratsystems.asistentepersonal;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
    static final int DEFAULT_NOTICE_MINUTES=30;
    final int BG=Color.rgb(8,17,31), CARD=Color.rgb(17,31,51), WHITE=Color.WHITE, BLUE=Color.rgb(25,118,210);
    LinearLayout root;
    TextRecognizer ocr;
    String what="", date="", time="";
    String sourceRaw="";

    static class Parsed {
        String what="";
        LocalDateTime event;
        LocalDateTime alert;
        int relativeNoticeMinutes=DEFAULT_NOTICE_MINUTES;
        boolean explicitAlert=false;
    }

    public void onCreate(Bundle b){
        super.onCreate(b);
        ocr=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        askPerms();
        home();
    }

    void askPerms(){
        ArrayList<String> p=new ArrayList<>();
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.RECORD_AUDIO);
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.CAMERA);
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.POST_NOTIFICATIONS);
        if(!p.isEmpty())requestPermissions(p.toArray(new String[0]),PERMS);
    }

    TextView t(String s,int z,boolean bold){
        TextView v=new TextView(this);
        v.setText(s);v.setTextColor(WHITE);v.setTextSize(z);v.setPadding(10,10,10,10);
        if(bold)v.setTypeface(null,1);
        return v;
    }

    Button b(String s){
        Button v=new Button(this);
        v.setText(s);v.setTextColor(WHITE);v.setTextSize(17);v.setAllCaps(false);v.setBackgroundColor(BLUE);v.setPadding(10,20,10,20);
        return v;
    }

    LinearLayout card(){
        LinearLayout c=new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);c.setPadding(18,16,18,16);c.setBackgroundColor(CARD);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(8,10,8,10);c.setLayoutParams(p);
        return c;
    }

    void base(String sub){
        ScrollView s=new ScrollView(this);s.setBackgroundColor(BG);
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(14,30,14,30);
        s.addView(root);root.addView(t("Asistente Personal",25,true));root.addView(t(sub,14,false));setContentView(s);
    }

    void home(){
        base("Dímelo una vez. Si entiendo qué, fecha y hora, lo programo solo.");
        LinearLayout c=card();c.addView(t("Entrada rápida",19,true));
        Button v=b("🎙️  Hablar"), cam=b("📷  Hacer foto"), gal=b("🖼️  Galería");
        c.addView(v);c.addView(cam);c.addView(gal);root.addView(c);
        v.setOnClickListener(x->voice());cam.setOnClickListener(x->camera());gal.setOnClickListener(x->gallery());

        String last=getPreferences(MODE_PRIVATE).getString("last_saved","");
        if(!last.isEmpty()){
            LinearLayout l=card();
            l.addView(t("Último recordatorio",17,true));
            l.addView(t(last,16,false));
            root.addView(l);
        }
    }

    void voice(){
        if(!SpeechRecognizer.isRecognitionAvailable(this)){toast("Reconocimiento de voz no disponible");return;}
        base("Habla con normalidad. No tendrás que rellenar nada si lo entiendo.");
        root.addView(t("🎙️ Te escucho…",24,true));
        SpeechRecognizer r=SpeechRecognizer.createSpeechRecognizer(this);
        r.setRecognitionListener(new RecognitionListener(){
            public void onReadyForSpeech(Bundle b){} public void onBeginningOfSpeech(){} public void onRmsChanged(float f){}
            public void onBufferReceived(byte[] b){} public void onEndOfSpeech(){} public void onPartialResults(Bundle b){}
            public void onEvent(int a,Bundle b){}
            public void onError(int e){toast("No lo he entendido. Inténtalo otra vez.");home();}
            public void onResults(Bundle b){
                ArrayList<String> a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(a!=null&&!a.isEmpty())process(a.get(0));
            }
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
            android.graphics.Bitmap bm=ex==null?null:(android.graphics.Bitmap)ex.get("data");
            if(bm!=null)read(InputImage.fromBitmap(bm,0)); else toast("No pude leer la foto");
        } else if(req==GAL){
            Uri u=data.getData();
            if(u!=null)try{read(InputImage.fromFilePath(this,u));}catch(IOException e){toast("No pude abrir la imagen");}
        }
    }

    void read(InputImage i){
        base("Leyendo la imagen…");
        root.addView(t("📷 Analizando texto, fecha y hora…",21,true));
        ocr.process(i).addOnSuccessListener(x->{
            String s=x.getText();
            if(s==null||s.trim().isEmpty()) askOnlyMissing("",null,null);
            else process(s);
        }).addOnFailureListener(x->askOnlyMissing("",null,null));
    }

    void process(String raw){
        sourceRaw=raw==null?"":raw;
        Parsed p=parse(sourceRaw);
        if(p.event!=null && p.what.length()>=3){
            scheduleParsed(p);
            return;
        }
        LocalDate d=p.event==null?extractDate(normalize(sourceRaw)):p.event.toLocalDate();
        LocalTime tm=p.event==null?extractTime(normalize(sourceRaw)):p.event.toLocalTime();
        askOnlyMissing(p.what,d,tm);
    }

    Parsed parse(String raw){
        Parsed p=new Parsed();
        String clean=raw.replace('\n',' ').replaceAll("\\s+"," ").trim();
        String n=normalize(clean);

        String eventText=n;
        String alertText="";
        Matcher split=Pattern.compile("\\b(?:avisame|avísame|avisar|avísar|recuerdame|recuérdame)\\b").matcher(n);
        if(split.find()){
            eventText=n.substring(0,split.start()).trim();
            alertText=n.substring(split.start()).trim();
        }

        LocalDate d=extractDate(eventText);
        LocalTime tm=extractTime(eventText);

        if(d!=null && tm!=null){
            LocalDateTime ev=LocalDateTime.of(d,tm);
            if(ev.isBefore(LocalDateTime.now()) && !containsExplicitAbsoluteDate(eventText)){
                if(eventText.matches(".*\\bhoy\\b.*")) {
                    // keep today: it is genuinely in the past and will be rejected later
                } else if(containsWeekday(eventText)) {
                    d=d.plusWeeks(1);
                    ev=LocalDateTime.of(d,tm);
                }
            }
            p.event=ev;
        }

        p.what=cleanWhat(clean);
        if(p.what.length()<3)p.what=clean;

        if(!alertText.isEmpty() && p.event!=null){
            Integer mins=parseRelativeNotice(alertText);
            if(mins!=null){
                p.relativeNoticeMinutes=mins;
                p.alert=p.event.minusMinutes(mins);
                p.explicitAlert=true;
            } else {
                LocalDate ad=extractDate(alertText);
                LocalTime at=extractTime(alertText);
                if(ad!=null && at!=null){
                    if(LocalDateTime.of(ad,at).isAfter(p.event) && containsWeekday(alertText)) ad=ad.minusWeeks(1);
                    p.alert=LocalDateTime.of(ad,at);
                    p.explicitAlert=true;
                }
            }
        }

        if(p.event!=null && p.alert==null)p.alert=p.event.minusMinutes(DEFAULT_NOTICE_MINUTES);
        return p;
    }

    String normalize(String s){
        return s.toLowerCase(new Locale("es","ES"))
            .replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u")
            .replaceAll("[,;]"," ")
            .replaceAll("\\s+"," ").trim();
    }

    boolean containsExplicitAbsoluteDate(String s){
        return s.matches(".*\\b\\d{1,2}[/-]\\d{1,2}(?:[/-]\\d{2,4})?\\b.*")
            || s.matches(".*\\b\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}\\b.*")
            || s.matches(".*\\b\\d{1,2}\\s+(?:de\\s+)?(?:enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|setiembre|octubre|noviembre|diciembre).*");
    }

    boolean containsWeekday(String s){
        return s.matches(".*\\b(lunes|martes|miercoles|jueves|viernes|sabado|domingo)\\b.*");
    }

    LocalDate extractDate(String s){
        LocalDate today=LocalDate.now();
        if(s.contains("pasado manana"))return today.plusDays(2);
        if(s.matches(".*\\bmanana\\b.*"))return today.plusDays(1);
        if(s.matches(".*\\bhoy\\b.*"))return today;

        Matcher iso=Pattern.compile("\\b(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})\\b").matcher(s);
        if(iso.find())try{return LocalDate.of(Integer.parseInt(iso.group(1)),Integer.parseInt(iso.group(2)),Integer.parseInt(iso.group(3)));}catch(Exception ignored){}

        Matcher d=Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?\\b").matcher(s);
        if(d.find()){
            int y=d.group(3)==null?today.getYear():Integer.parseInt(d.group(3));
            if(y<100)y+=2000;
            try{
                LocalDate parsed=LocalDate.of(y,Integer.parseInt(d.group(2)),Integer.parseInt(d.group(1)));
                if(d.group(3)==null&&parsed.isBefore(today))parsed=parsed.plusYears(1);
                return parsed;
            }catch(Exception ignored){}
        }

        String months="enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|setiembre|octubre|noviembre|diciembre";
        Matcher md=Pattern.compile("\\b(\\d{1,2})\\s+(?:de\\s+)?("+months+")(?:\\s+(?:de\\s+)?(\\d{4}))?\\b").matcher(s);
        if(md.find()){
            int month=monthNumber(md.group(2));
            int year=md.group(3)==null?today.getYear():Integer.parseInt(md.group(3));
            try{
                LocalDate parsed=LocalDate.of(year,month,Integer.parseInt(md.group(1)));
                if(md.group(3)==null&&parsed.isBefore(today))parsed=parsed.plusYears(1);
                return parsed;
            }catch(Exception ignored){}
        }

        Map<String,DayOfWeek> days=new LinkedHashMap<>();
        days.put("lunes",DayOfWeek.MONDAY);days.put("martes",DayOfWeek.TUESDAY);days.put("miercoles",DayOfWeek.WEDNESDAY);
        days.put("jueves",DayOfWeek.THURSDAY);days.put("viernes",DayOfWeek.FRIDAY);days.put("sabado",DayOfWeek.SATURDAY);days.put("domingo",DayOfWeek.SUNDAY);
        for(Map.Entry<String,DayOfWeek> e:days.entrySet())
            if(s.matches(".*\\b"+e.getKey()+"\\b.*"))return today.with(TemporalAdjusters.nextOrSame(e.getValue()));
        return null;
    }

    LocalTime extractTime(String s){
        Matcher tm=Pattern.compile("(?i)(?:a\\s+las?|a\\s+la|sobre\\s+las?|hora\\s*)?\\b([01]?\\d|2[0-3])[:.h]([0-5]\\d)\\b(?:\\s*(?:de\\s+la\\s+)?(manana|tarde|noche))?").matcher(s);
        if(tm.find()){
            int h=Integer.parseInt(tm.group(1)),m=Integer.parseInt(tm.group(2));
            h=applyDayPart(h,tm.group(3));
            try{return LocalTime.of(h,m);}catch(Exception ignored){}
        }

        Matcher num=Pattern.compile("(?i)(?:a\\s+las?|a\\s+la|sobre\\s+las?)\\s+([01]?\\d|2[0-3])(?:\\s*(?:h|horas?))?(?:\\s+(?:de\\s+la\\s+)?(manana|tarde|noche))?\\b").matcher(s);
        if(num.find()){
            int h=Integer.parseInt(num.group(1));
            h=applyDayPart(h,num.group(2));
            try{return LocalTime.of(h,0);}catch(Exception ignored){}
        }

        Matcher words=Pattern.compile("(?i)(?:a\\s+las?|a\\s+la|sobre\\s+las?)\\s+(una|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce|trece|catorce|quince|dieciseis|diecisiete|dieciocho|diecinueve|veinte|veintiuna|veintiuno|veintidos|veintitres)(?:\\s+y\\s+(cuarto|media|\\d{1,2}))?(?:\\s+(?:de\\s+la\\s+)?(manana|tarde|noche))?\\b").matcher(s);
        if(words.find()){
            int h=wordHour(words.group(1)),m=0;
            if(words.group(2)!=null){if(words.group(2).equals("cuarto"))m=15;else if(words.group(2).equals("media"))m=30;else m=Integer.parseInt(words.group(2));}
            h=applyDayPart(h,words.group(3));
            try{return LocalTime.of(h,m);}catch(Exception ignored){}
        }
        return null;
    }

    int applyDayPart(int h,String part){
        if(part==null)return h;
        if(part.equals("tarde") && h>=1 && h<=7)return h+12;
        if(part.equals("noche") && h>=1 && h<=11)return h+12;
        if(part.equals("manana") && h==12)return 0;
        return h;
    }

    Integer parseRelativeNotice(String s){
        Matcher m=Pattern.compile("\\b(\\d+)\\s*(minuto|minutos|hora|horas|dia|dias)\\s+antes\\b").matcher(s);
        if(m.find()){
            int n=Integer.parseInt(m.group(1));
            String u=m.group(2);
            if(u.startsWith("minuto"))return n;
            if(u.startsWith("hora"))return n*60;
            return n*1440;
        }
        if(s.matches(".*\\bmedia\\s+hora\\s+antes\\b.*"))return 30;
        if(s.matches(".*\\buna?\\s+hora\\s+antes\\b.*"))return 60;
        if(s.matches(".*\\bun\\s+dia\\s+antes\\b.*"))return 1440;
        if(s.matches(".*\\bdos\\s+horas\\s+antes\\b.*"))return 120;
        return null;
    }

    String cleanWhat(String raw){
        String s=normalize(raw);
        int alertPos=-1;
        Matcher alert=Pattern.compile("\\b(?:avisame|avisar)\\b").matcher(s);
        if(alert.find())alertPos=alert.start();
        if(alertPos>=0)s=s.substring(0,alertPos);

        s=s.replaceAll("\\b(recu[eé]rdame|recuerdame|tengo que|hay que|debo|necesito|cita para|tengo cita para)\\b"," ");
        s=s.replaceAll("\\b(pasado manana|manana|hoy)\\b"," ");
        s=s.replaceAll("\\b(lunes|martes|miercoles|jueves|viernes|sabado|domingo)\\b"," ");
        s=s.replaceAll("\\b\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}\\b"," ");
        s=s.replaceAll("\\b\\d{1,2}[/-]\\d{1,2}(?:[/-]\\d{2,4})?\\b"," ");
        s=s.replaceAll("\\b\\d{1,2}\\s+(?:de\\s+)?(?:enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|setiembre|octubre|noviembre|diciembre)(?:\\s+(?:de\\s+)?\\d{4})?\\b"," ");
        s=s.replaceAll("(?i)(?:a\\s+las?|a\\s+la|sobre\\s+las?)\\s+(?:[01]?\\d|2[0-3])(?::[0-5]\\d)?(?:\\s*(?:h|horas?))?(?:\\s+(?:de\\s+la\\s+)?(?:manana|tarde|noche))?"," ");
        s=s.replaceAll("(?i)(?:a\\s+las?|a\\s+la|sobre\\s+las?)\\s+(?:una|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce|trece|catorce|quince|dieciseis|diecisiete|dieciocho|diecinueve|veinte|veintiuna|veintiuno|veintidos|veintitres)(?:\\s+y\\s+(?:cuarto|media|\\d{1,2}))?(?:\\s+(?:de\\s+la\\s+)?(?:manana|tarde|noche))?"," ");
        s=s.replaceAll("\\s+"," ").trim();
        if(s.startsWith("el "))s=s.substring(3).trim();
        return s;
    }

    int monthNumber(String m){
        String[] a={"enero","febrero","marzo","abril","mayo","junio","julio","agosto","septiembre","octubre","noviembre","diciembre"};
        if(m.equals("setiembre"))m="septiembre";
        for(int i=0;i<a.length;i++)if(a[i].equals(m))return i+1;
        return 0;
    }

    int wordHour(String s){
        String[] w={"","una","dos","tres","cuatro","cinco","seis","siete","ocho","nueve","diez","once","doce","trece","catorce","quince","dieciseis","diecisiete","dieciocho","diecinueve","veinte","veintiuna","veintidos","veintitres"};
        for(int i=1;i<w.length;i++)if(w[i].equals(s))return i;
        if(s.equals("veintiuno"))return 21;
        return -1;
    }

    void askOnlyMissing(String initialWhat,LocalDate d,LocalTime tm){
        what=initialWhat==null?"":initialWhat;
        date=d==null?"":d.toString();
        time=tm==null?"":String.format(Locale.US,"%02d:%02d",tm.getHour(),tm.getMinute());

        if(what.length()<3){
            EditText e=new EditText(this);
            new AlertDialog.Builder(this).setTitle("Solo me falta una cosa").setMessage("¿Qué tengo que recordar?").setView(e).setCancelable(false)
                .setPositiveButton("Guardar",(x,w)->{what=e.getText().toString().trim();askOnlyMissing(what,d,tm);}).show();
            return;
        }
        if(d==null){
            final String keepWhat=what;
            new AlertDialog.Builder(this).setTitle("Solo me falta la fecha").setMessage("No he encontrado una fecha clara.")
                .setPositiveButton("Elegir fecha",(x,w)->{
                    LocalDate n=LocalDate.now();
                    new DatePickerDialog(this,(v,y,m,day)->askOnlyMissing(keepWhat,LocalDate.of(y,m+1,day),tm),n.getYear(),n.getMonthValue()-1,n.getDayOfMonth()).show();
                }).setNegativeButton("Cancelar",(x,w)->home()).show();
            return;
        }
        if(tm==null){
            final String keepWhat=what; final LocalDate keepDate=d;
            new AlertDialog.Builder(this).setTitle("Solo me falta la hora").setMessage("No he encontrado una hora clara.")
                .setPositiveButton("Elegir hora",(x,w)->{
                    LocalTime n=LocalTime.now();
                    new TimePickerDialog(this,(v,h,m)->askOnlyMissing(keepWhat,keepDate,LocalTime.of(h,m)),n.getHour(),n.getMinute(),true).show();
                }).setNegativeButton("Cancelar",(x,w)->home()).show();
            return;
        }

        Parsed p=new Parsed();
        p.what=what;p.event=LocalDateTime.of(d,tm);p.alert=p.event.minusMinutes(DEFAULT_NOTICE_MINUTES);
        scheduleParsed(p);
    }

    void scheduleParsed(Parsed p){
        if(p.event==null){toast("No he encontrado fecha y hora");return;}
        if(p.event.isBefore(LocalDateTime.now())){toast("La fecha u hora ya ha pasado");home();return;}
        if(p.alert==null)p.alert=p.event.minusMinutes(DEFAULT_NOTICE_MINUTES);
        if(p.alert.isBefore(LocalDateTime.now()))p.alert=LocalDateTime.now().plusSeconds(5);
        if(p.alert.isAfter(p.event)){toast("El aviso no puede ser después del evento");home();return;}

        try{
            long when=p.alert.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            Intent i=new Intent(this,AlarmReceiver.class);
            i.putExtra("what",p.what);
            i.putExtra("event",formatEvent(p.event));
            PendingIntent pi=PendingIntent.getBroadcast(this,(int)(System.currentTimeMillis()%Integer.MAX_VALUE),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
            if(Build.VERSION.SDK_INT>=31 && am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);

            String summary="✓ "+capitalize(p.what)+"\nEvento: "+formatEvent(p.event)+"\nAviso: "+formatEvent(p.alert);
            getPreferences(MODE_PRIVATE).edit().putString("last_saved",summary).apply();
            showSaved(summary);
        }catch(Exception e){
            toast("No pude programar el aviso");
            home();
        }
    }

    String formatEvent(LocalDateTime dt){
        return String.format(Locale.US,"%02d/%02d/%04d · %02d:%02d",dt.getDayOfMonth(),dt.getMonthValue(),dt.getYear(),dt.getHour(),dt.getMinute());
    }

    String capitalize(String s){
        if(s==null||s.isEmpty())return "";
        return s.substring(0,1).toUpperCase()+s.substring(1);
    }

    void showSaved(String summary){
        base("Hecho. Ya está programado.");
        LinearLayout c=card();
        c.addView(t(summary,19,true));
        c.addView(t("No tienes que hacer nada más.",16,false));
        Button ok=b("Aceptar");
        c.addView(ok);root.addView(c);
        ok.setOnClickListener(v->home());
    }

    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public void onBackPressed(){home();}
}
