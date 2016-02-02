package com.andrey.kostin.clockwordswidget;
/*
onEnabled() — вызывается один раз, когда виджет был добавлен на рабочий стол
onDisabled() — вызывается, когда последний экземпляр виджета был удалён с рабочего стола
onUpdate() — вызывается при каждом обновлении виджета.
onDeleted() — вызывается, когда виджет удаляется с рабочего стола
*/

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.andrey.kostin.timewidget.R;


public class Clockwordswidget extends AppWidgetProvider {

    final static String LOG_TAG = "MYLOG";      //константа для вывода логов
    public static final String FORCE_UPDATE_WIDGET = "com.andrey.kostin.timewidget.FORCE_UPDATE"; //константа для обновления через пендингинтент
    long starttime;                             //переменная для хранения начального времени запуска алармменеджера
    static int textcolor,background;            //переменные цвета текста и бекграунда будут заполнятся из преференсес
//    static boolean foundClockImpl;              //переменная для определения наличия системного приложения будильника
//    static Intent alarmClockIntent;             //Интент будет использоваться для вызова приложения будильника
//    static PendingIntent palarm;                //переменая для пендингинтента вызова будильника
//    static PackageManager packageManager;       //переменная пакаджменеджера будет использоваться для использования встроенного приложения будильника

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(LOG_TAG, "onEnabled");

     // Обновляем виджет алармменеджером
        AlarmManager alm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        starttime = (System.currentTimeMillis() / 60000) * 60000 + 60200;               //Начальное время alarmManager задаем так, чтобы обновления происходили когда системное время начинает отсчет следующей минуты, чтобы время на виджете менялось на 0.2 секунды позже системных частов
        alm.setRepeating(AlarmManager.RTC, starttime, 60000, getPendingIntent(context)); //для обновления используем пендингинтент из функции гетпендингинтент



    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
//        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));
        SharedPreferences sp = context.getSharedPreferences(Config.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int id : appWidgetIds) {updateWidget(context, appWidgetManager, sp, id);}        //вызываем метод обновления внешнего вида каждого виджета функцией updateWidget
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
        // Удаляем Preferences
        SharedPreferences.Editor editor = context.getSharedPreferences(Config.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) { editor.remove(Config.TEXT_COLOR + widgetID);
                                            editor.remove(Config.BACK_COLOR + widgetID);}
        editor.commit();
    }

    //Функция для создания пендингинтента для работы с обновлением виджета аларм менеджером
    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, getClass());
        intent.setComponent(new ComponentName(context, this.getClass()));
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
        //Кусок для удаления обновления через алармменеджер
        AlarmManager alm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alm.cancel(getPendingIntent(context)); //для обновления используем пендингинтент из функции гетперндингинтент
    }

   @Override //Ресивер описываем для обновления через алармы
    public void onReceive(Context context, Intent intent) {
        if (FORCE_UPDATE_WIDGET.equals(intent.getAction()) || AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager apwm = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = apwm.getAppWidgetIds(new ComponentName(context, getClass()));
            onUpdate(context, apwm, appWidgetIds);
        } else
            super.onReceive(context, intent);
    }





    public static String doNumberToWord(Context context, Boolean flagRU){                      //Функция возвращающая текущее время в строковую переменную словами

        String finaltimestring="";                                 //переменная для вывода времени словами
        SimpleDateFormat dtime;                              //переменная для задания формата часов, минут и указателя AM/PM
        String timeitem, hourAM, hourhvost="", minutecountALL="",minutecountHI="",minutecountLO="",minutevivod="", minutehvost="";     // переменные для вывода времени из симплдэйтформата, указателя, слова "часов", количества минут, слова "минут"

        //String[] hourarr={"НОЛЬ","ОДИН","ДВА","ТРИ","ЧЕТЫРЕ","ПЯТЬ","ШЕСТЬ","СЕМЬ","ВОСЕМЬ","ДЕВЯТЬ","ДЕСЯТЬ","ОДИННАДЦАТЬ","ДВЕНАДЦАТЬ","ЧАС"};            //массив текстовых значений часов
        //String[] hourGOarr={"НОЛЬ","ПЕРВОГО","ВТОРОГО","ТРЕТЬЕГО","ЧЕТВЕРТОГО","ПЯТОГО","ШЕСТОГО","СЕДЬМОГО","ВОСЬМОГО","ДЕВЯТОГО","ДЕСЯТОГО","ОДИННАДЦАТОГО","ДВЕНАДЦАТОГО","ПЕРВОГО"}; //массив для фраз Половина Первого, Второго...
        //String[] hourhvostarr={"ЧАСОВ","ЧАС","ЧАСА"};           //массив форм слова "час"
        //String[] minutehvostarr={"МИНУТ","МИНУТА","МИНУТЫ"};    //массив форм слова "минут"
        //String[] slang={"РОВНО","ПОЛОВИНА","БЕЗ ДВАДЦАТИ","БЕЗ ЧЕТВЕРТИ","БЕЗ ДЕСЯТИ","БЕЗ ПЯТИ","ПОСЛЕ","ДО","ПОЛНОЧЬ"};   //массив общеупотребительных способов говорить время
        //String[] minutearr={"НОЛЬ","ОДНА", "ДВЕ", "ТРИ", "ЧЕТЫРЕ", "ПЯТЬ", "ШЕСТЬ", "СЕМЬ", "ВОСЕМЬ", "ДЕВЯТЬ","ДЕСЯТЬ","ОДИННАДЦАТЬ","ДВЕНАДЦАТЬ","ТРИНАДЦАТЬ","ЧЕТЫРНАДЦАТЬ","ПЯТНАДЦАТЬ","ШЕСТНАДЦАТЬ","СЕМНАДЦАТЬ","ВОСЕМНАДЦАТЬ","ДЕВЯТНАДЦАТЬ","ДВАДЦАТЬ"};//массив текстовых значений минут
        //String[] minuteHIarr={"","", "ДВАДЦАТЬ", "ТРИДЦАТЬ", "СОРОК", "ПЯТЬДЕСЯТ"};                             //массив текстовых значений старшего разряда минут
        //String[] minuteLOarr={"","ОДНА", "ДВЕ", "ТРИ", "ЧЕТЫРЕ", "ПЯТЬ", "ШЕСТЬ", "СЕМЬ", "ВОСЕМЬ", "ДЕВЯТЬ"};  //массив текстовых значений младшего разряда минут

        String[] hourarr = context.getResources().getStringArray(R.array.hourarr);                  //массив текстовых значений часов
        String[] hourGOarr = context.getResources().getStringArray(R.array.hourGOarr);              //массив для фраз Половина Первого, Второго...
        String[] hourhvostarr = context.getResources().getStringArray(R.array.hourhvostarr);        //массив форм слова "час"
        String[] minutehvostarr = context.getResources().getStringArray(R.array.minutehvostarr);    //массив форм слова "минут"
        String[] slang = context.getResources().getStringArray(R.array.slang);                      //массив общеупотребительных способов говорить время
        String[] minutearr = context.getResources().getStringArray(R.array.minutearr);              //массив текстовых значений минут c 0 до 20
        String[] minuteHIarr = context.getResources().getStringArray(R.array.minuteHIarr);          //массив текстовых значений старшего разряда минут
        String[] minuteLOarr = context.getResources().getStringArray(R.array.minuteLOarr);          //массив текстовых значений младшего разряда минут

        dtime = new SimpleDateFormat("h mm a");                         //Задаем формат часов 12 без лидирующих нулей , минуты с лидирующими нулями, указатель AM/PM через пробел
        timeitem= dtime.format(Calendar.getInstance().getTime());       //получаем часы минуты указатель в строку разделенную пробелом
        String time[]=timeitem.split(" ");                              //заносим в массив значения разделенные пробелом time[0]=12 часы time[1]=23 минуты time[2]=AM указатель

        int hourint=Integer.valueOf(time[0]);                   //Переменная int текущий час
        hourAM=time[2];                                         //Переменная стринг указатель AM/PM

        switch(hourint){                                        //выбираем соответствующую форму слова "час" которую берем из массива hourhvostarr
            case 0:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12: hourhvost=hourhvostarr[0]; break;          //часов
            case 1:  hourhvost=hourhvostarr[1]; break;          //час
            case 2:
            case 3:
            case 4:  hourhvost=hourhvostarr[2]; break;          //часа
        }

        minutecountALL =time[1];                                //передаем минуты из массива в строковую переменную
        minutecountHI = minutecountALL.substring(0,1);          //передаем первый разряд минут в строковую переменную
        minutecountLO = minutecountALL.substring(1);            //передаем второй разряд минут в строковую переменную
        int minuteintALL=Integer.valueOf(minutecountALL);       //Переменная для работы с общим числом минут
        int minuteintHI=Integer.valueOf(minutecountHI);         //Переменная для работы с первым разрядом минут
        int minuteintLO=Integer.valueOf(minutecountLO);         //Переменная для работы в вторым разрядом минут

        if(minuteintALL<20){                                    //анализируем полученное число. Если меньше 20 то:
            minutevivod=minutearr[minuteintALL];                //В переменную минутвывод заносим значение из массива minutearr
        }else{                                                  //иначе:
            minutevivod=minuteHIarr[minuteintHI]+" "+minuteLOarr[minuteintLO];}//В переменную минутвывод заносим значение десятков из массива старшего разряда minuteHIarr(значение младшего разряда)
        //плюс значение единиц из массива minuteLOarr[значение младшего разряда]
        if(minuteintHI==1){minutehvost=minutehvostarr[0];}      //если 10-19 ставим окончание  "минут",
        else{                                                   //иначе выбираем с помощью свитч:
            switch(minuteintLO){                                //выбираем соответствующую форму слова "минут" из массива minutehvostarr в зависимости от второго разряда
                case 0:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9: minutehvost=minutehvostarr[0];break;    //минут
                case 1: minutehvost=minutehvostarr[1];break;    //минута
                case 2:
                case 3:
                case 4: minutehvost=minutehvostarr[2];break;    //минуты
            }
        }
        Log.d(LOG_TAG,"Locale RU = "+flagRU+" minutecountHI = "+minuteintHI+" minutecountLO = "+minuteintLO +" minutevivod = "+minutevivod+" minutehvost="+ minutehvost+" hourAM= "+hourAM );

        if (flagRU){        //если локаль русская, формируем финальную фразу по правилам русского языка
         switch(minuteintALL) {                                  //В зависимости от значения минут формируем финальную фразу, сообщающую время.
            case 0:  if(time[2].equals("после")&&(hourint==8))  finaltimestring = slang[9];
                                                        else finaltimestring = hourarr[hourint]+ " " + hourhvost+ " " + slang[0]; break;            //ровно
            case 5:
            case 10:
            case 15:
            case 20:
            case 25: finaltimestring = minutevivod + " " + minutehvost + " " + hourGOarr[hourint+1]; break;
            case 30: finaltimestring = slang[1]+" "+ hourGOarr[hourint+1]; break;               //Половина
            case 40: finaltimestring = slang[2]+" "+ hourarr[hourint+1]; break;   //Без двадцати
            case 45: finaltimestring = slang[3]+" "+ hourarr[hourint+1]; break;   //Без четверти
            case 50: finaltimestring = slang[4]+" "+ hourarr[hourint+1]; break;   //Без десяти
            case 55: finaltimestring = slang[5]+" "+ hourarr[hourint+1]; break;   //Без пяти
            default: finaltimestring = hourarr[hourint] + " " + hourhvost + " " + minutevivod + " " + minutehvost; break;//по умолчанию выводим сформированный по правилам формат времени
         }
        }else{          //иначе формируем финальную фразу по правилам английского языка
            switch(minuteintALL) {                                  //В зависимости от значения минут формируем финальную фразу, сообщающую время.
                case 0:  finaltimestring = hourarr[hourint]+ " " + slang[0]; break;            //ровно Two o’clock
                case 15: finaltimestring = slang[3]+ " " + slang[6]+ " "  + hourarr[hourint]; break;         //Quarter past ten
                case 5:
                case 10:
                case 20:
                case 25: finaltimestring = minutevivod + " "+ minutehvost + " " + slang[6]+ " " + hourarr[hourint]; break; //five minutes past eleven
                case 30: finaltimestring = slang[1]+ " "+slang[7]+ " " + hourarr[hourint+1]; break;   //Половина    Half to eleven
                case 40: finaltimestring = slang[2]+ " "+slang[7]+ " " + hourarr[hourint+1]; break;   //Без двадцати
                case 45: finaltimestring = slang[3]+ " "+slang[7]+ " " + hourarr[hourint+1]; break;   //Без четверти Quarter to three
                case 50: finaltimestring = slang[4]+ " "+slang[7]+ " " + hourarr[hourint+1]; break;   //Без десяти
                case 55: finaltimestring = slang[5]+ " "+slang[7]+ " " + hourarr[hourint+1]; break;   //Без пяти
                case 58: if(time[2].equals("PM")&&(hourint==11)) finaltimestring = minutearr[2]+ " " + minutehvost + " " + slang[7]+ " " + slang[8]; break;   //two minutes to midnight
                default: finaltimestring = hourarr[hourint] + " " + minutevivod + " " + time[2]; break;//по умолчанию выводим сформированный по правилам формат времени four fifty five pm
            }
        }

            Log.d(LOG_TAG,"numminutes = " + minuteintALL + " widtime= " + finaltimestring );

        return finaltimestring;                             //возвращаем полученное время
    }

    public static Bitmap getFontBitmap(Context context, String text, int color, float fontSizeSP) {
        int fontSizePX = convertDiptoPix(context, fontSizeSP);
        int pad = (fontSizePX / 9);
        Paint paint = new Paint();
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "sansita.ttf");
        paint.setAntiAlias(true);
        paint.setTypeface(typeface);
        paint.setColor(color);
        paint.setTextSize(fontSizePX);

        int textWidth = (int) (paint.measureText(text) + pad * 2);
        int height = (int) (fontSizePX / 0.75);
        Bitmap bitmap = Bitmap.createBitmap(textWidth, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        float xOriginal = pad;
        canvas.drawText(text, xOriginal, fontSizePX, paint);
        return bitmap;
    }

    public static int convertDiptoPix(Context context, float dip) {
        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
        return value;
    }


    static void updateWidget(Context context, AppWidgetManager appWidgetManager,SharedPreferences sp, int widgetID) { //метод выполняется внутри метода Онапдейт
//        Log.d(LOG_TAG, "updateWidget " + widgetID);
        Boolean flagRU = sp.getBoolean(Config.LANG_FLAG + widgetID, false);
        String widtime = doNumberToWord(context,flagRU);          //Вызываем функцию возвращающую время словами(передаем ей контекст и флаг языка), полученное значение заносим в widtime


        String widdate, nextAlarm;                  //переменные для вывода даты и времени следующего будильникаt
        SimpleDateFormat df;
        df = new SimpleDateFormat("EEEE, d MMMM");    //Задаем формат даты среда, 9 декабря
        widdate = df.format(Calendar.getInstance().getTime());       //передаем дату-время в строковую переменную

        //Узнаем когда сработает следующий будильник и заносим значение в переменную нексталарм
        nextAlarm = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);//добавляем информацию о ближайшем аларме. Можно еще "\u23F0 "+ размещаем в строке символ юникод будильник
        if(nextAlarm.length()==0){nextAlarm=nextAlarm+context.getString(R.string.alarmisset);}//если будильники не установлены(тоесть строка нексталарм содержит всего два символа - будильник и пробел) то выводим фразу Аларм нот сет

        /*String widdate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault() ).format(new Date()); //ради разнообразия можно задать дату одной строкой через метод гетДатеИнстанс Locale.getDefault() - формат даты по умолчанию для установленного языкового пакета Locale.English например
        SHORT is completely numeric, such as 12.13.52 or 3:30pm
        MEDIUM is longer, such as Jan 12, 1952
        LONG is longer, such as January 12, 1952 or 3:30:32pm
        FULL is pretty completely specified, such as Tuesday, April 12, 1952 AD or 3:30:42pm PST.
        */

        /*Читаем параметры даты и времени из Preferences - сейчас не использую потому как выше задаю напрямую
        String widtime = sp.getString(Config.WIDTIME + widgetID, null);
        if (widtime == null) return;
        String widdate = sp.getString(Config.WIDDATE + widgetID, null);
        if (widdate == null) return;*/

        // Настраиваем внешний вид виджета, помещаем данные в текстовые поля
        RemoteViews widgetView = new RemoteViews(context.getPackageName(),R.layout.widget);
        widgetView.setTextViewText(R.id.time, widtime);     //!!!именно здесь передаем текст из переменной в виджет в текствью тайм
        widgetView.setTextViewText(R.id.date, widdate);     //!!!именно здесь передаем текст из переменной в виджет в текствью дэйт
        widgetView.setTextViewText(R.id.alarm, nextAlarm);  //!!!именно здесь передаем текст из переменной в виджет в текствью аларм

        //Читаем цвет текста из преференсес
        textcolor = sp.getInt(Config.TEXT_COLOR + widgetID, Color.WHITE); //берем цвет текста из преференсес, если цвет не указан выводим белый (Color.parseColor("#ffffff"))

        widgetView.setTextColor(R.id.time, textcolor);      //!!!задаем цвет текста указаный в конфиг активити надписям
        widgetView.setTextColor(R.id.date, textcolor);
        widgetView.setTextColor(R.id.alarm, textcolor);

        //Читаем фон лайота из преференсес
        background = sp.getInt(Config.BACK_COLOR + widgetID, 0);                //заносим в переменную цвет фона лайота из преференсес
        widgetView.setInt(R.id.widlayout, "setBackgroundResource", background); //устанавливаем фон из переменной лайоту виджета
        widgetView.setInt(R.id.icon, "setColorFilter",textcolor );              //устанавливаем цвет имеджвью будильника
        //widgetView.setInt(R.id.icon, "setAlpha",Color.alpha(textcolor) );     //устанавливаем прозрачность имеджвью будильника
        //widgetView.setInt(R.id.widlayout, "setBackgroundColor", textcolor);   // здесь задаем цвет бекграунда лайота

        //String time = (String) DateFormat.format(mTimeFormat, mCalendar);
        //RemoteViews views = new RemoteViews(getPackageName(), R.layout.main);

       //!!! Выводим текст картинкой
        widgetView.setImageViewBitmap(R.id.imgtext, getFontBitmap(context, widtime, textcolor, 15));
        widgetView.setImageViewBitmap(R.id.imgdate, getFontBitmap(context, widdate, textcolor, 15));
        widgetView.setImageViewBitmap(R.id.imgalarm, getFontBitmap(context, nextAlarm, textcolor, 15));

/*      // Настройка вызова конфигурационного экрана по нажатию на лайот - должно открываться конфигурационное Activity. Создаем Intent, который будет вызывать Config Activity, помещаем данные об ID (чтобы экран знал, какой экземпляр он настраивает), упаковываем в PendingIntent и сопоставляем view-компоненту гаечному ключу.
        Intent confIntent = new Intent(context, Config.class);//создаем интент в который помещаем вызов конфигактивити
        confIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        confIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, confIntent, 0); //Intent мы упаковываем в PendingIntent
        widgetView.setOnClickPendingIntent(R.id.widlayout, pIntent);      //конкретному view-компоненту мы методом setOnClickPendingIntent сопоставляем PendingIntent. И когда будет совершено нажатие на этот view, система достанет Intent из PendingIntent и отправит его по назначению

        // Обновление виджета по нажатию на лайот
        Intent upIntent = new Intent(context, Clockwordswidget.class);
        upIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        upIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetID});
        pIntent = PendingIntent.getBroadcast(context, widgetID, upIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.widlayout, pIntent);

        //Настройка вызова будильника новой задачи будильника по нажатию на часы
        Intent alarmintent = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarmintent.putExtra(AlarmClock.EXTRA_MESSAGE, "New Alarm");
        alarmintent.putExtra(AlarmClock.EXTRA_HOUR, 10);
        alarmintent.putExtra(AlarmClock.EXTRA_MINUTES, 30);
        PendingIntent palarm = PendingIntent.getActivity(context, 0, alarmintent, 0); //Intent мы упаковываем в PendingIntent
        widgetView.setOnClickPendingIntent(R.id.time, palarm);      //конкретному view-компоненту мы методом setOnClickPendingIntent сопоставляем PendingIntent. И когда будет совершено нажатие на этот view, система достанет Intent из PendingIntent и отправит его по назначению
*/


        //Вызов встроенного приложения часов-будильника по нажатию на часы или время следующего будильника если приложение будильника программой обнаружено

         PendingIntent palarm;
         Boolean foundClockImpl = sp.getBoolean(Config.ALARM_FLAG + widgetID, false);                                   //берем флаг выбора приложения будильника из шаредпреференсес
//         Log.d(LOG_TAG, "foundClockImpl = "+ foundClockImpl);
         Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

        if (foundClockImpl){String packageName = sp.getString(Config.PAKAGE_NAME + widgetID, " ");
                            String className = sp.getString(Config.CLASS_NAME + widgetID, " ");
//            Log.d(LOG_TAG, "packageName = "+ packageName + " className = "+ className);
                            ComponentName cn = new ComponentName(packageName, className);
                            alarmClockIntent.setComponent(cn);      //здесь в интент помещаем имя пакаджа будильника и класса будильника для дальнейшего использования
                            palarm = PendingIntent.getActivity(context, 0, alarmClockIntent, 0);}   //Если флаг установлен - используем найденное приложение будильника
                      else {alarmClockIntent = new Intent(AlarmClock.ACTION_SET_ALARM);             //Настройка вызова встроенного приложения часов-будильника вторым способом - через интент аламклоку
                            alarmClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            palarm = PendingIntent.getActivity(context, widgetID, alarmClockIntent, 0);} //Intent мы упаковываем в PendingIntent, конкретному view-компоненту мы методом setOnClickPendingIntent сопоставляем PendingIntent. И когда будет совершено нажатие на этот view, система достанет Intent из PendingIntent и отправит его по назначению
        widgetView.setOnClickPendingIntent(R.id.widlayout, palarm);   //устанавливаем вызов будильника по нажатию на лайот

/*        //Настройка вызова встроенного приложения часов-будильника по нажатию на часы или время следующего будильника
        Intent alarmintent = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarmintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//      context.startActivity(alarmintent);                     //эта строка нужна, если вызывать интент просто без виджета, без пендингинтента
        PendingIntent palarm = PendingIntent.getActivity(context, widgetID, alarmintent, 0); //Intent мы упаковываем в PendingIntent
        widgetView.setOnClickPendingIntent(R.id.time, palarm);      //конкретному view-компоненту мы методом setOnClickPendingIntent сопоставляем PendingIntent. И когда будет совершено нажатие на этот view, система достанет Intent из PendingIntent и отправит его по назначению
        widgetView.setOnClickPendingIntent(R.id.alarm, palarm);     //устанавливаем вызов будильника по нажатию на текствью аларм
*/
/*      Настройка вызова браузера по нажатию на дату
        Intent internet = new Intent();
        internet.setAction(Intent.ACTION_VIEW);
        internet.addCategory(Intent.CATEGORY_BROWSABLE);
        internet.setData(Uri.parse("http:\\www.google.com"));
        pIntent = PendingIntent.getActivity(context, widgetID, internet, 0);
        widgetView.setOnClickPendingIntent(R.id.date, pIntent);
*/

        // Обновляем виджет
        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }
}