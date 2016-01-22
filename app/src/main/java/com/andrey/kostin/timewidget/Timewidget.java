package com.andrey.kostin.timewidget;
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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;


public class Timewidget extends AppWidgetProvider {

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
        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));
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

    static void updateWidget(Context context, AppWidgetManager appWidgetManager,SharedPreferences sp, int widgetID) { //метод выполняется внутри метода Онапдейт
        Log.d(LOG_TAG, "updateWidget " + widgetID);

        // Определяем текущее время непосредственно в теле таймвиджета
        SimpleDateFormat df;                 //переменная для задания формата времени
        String widtime, widdate, nextAlarm;  //переменные для вывода даты, времени и времени следующего будильника

        df = new SimpleDateFormat("HH:mm");                //Задаем формат времени  09:46
        widtime = df.format(Calendar.getInstance().getTime());       //передаем время в строковую переменную
        df = new SimpleDateFormat("EEE, d MMM");                            //Задаем формат даты           среда, 9 декабря
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

        //widgetView.setInt(R.id.time, "setBackgroundColor", widсolor);// здесь задаем цвет бекграунда текствью тайм

/*      // Настройка вызова конфигурационного экрана по нажатию на лайот - должно открываться конфигурационное Activity. Создаем Intent, который будет вызывать Config Activity, помещаем данные об ID (чтобы экран знал, какой экземпляр он настраивает), упаковываем в PendingIntent и сопоставляем view-компоненту гаечному ключу.
        Intent confIntent = new Intent(context, Config.class);//создаем интент в который помещаем вызов конфигактивити
        confIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        confIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, confIntent, 0); //Intent мы упаковываем в PendingIntent
        widgetView.setOnClickPendingIntent(R.id.widlayout, pIntent);      //конкретному view-компоненту мы методом setOnClickPendingIntent сопоставляем PendingIntent. И когда будет совершено нажатие на этот view, система достанет Intent из PendingIntent и отправит его по назначению

        // Обновление виджета по нажатию на лайот
        Intent upIntent = new Intent(context, Timewidget.class);
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
         Log.d(LOG_TAG, "foundClockImpl = "+ foundClockImpl);
         Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

        if (foundClockImpl){String packageName = sp.getString(Config.PAKAGE_NAME + widgetID, " ");
                            String className = sp.getString(Config.CLASS_NAME + widgetID, " ");
            Log.d(LOG_TAG, "packageName = "+ packageName + " className = "+ className);
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