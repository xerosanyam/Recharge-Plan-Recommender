# DataUsage
* Application to curate Wifi & Cellular Data from an Android Phone  
* Aim is to collect internet consumption data of users  and examine their usage pattern by plotting a graph between Internet Consumption(wifi and cellular) vs the Time of day(Morning, Evening, Night, etc)  

## Architecture
* Data is stored in SQLite and synced onced a day using Android Apis like AlarmManager & BroadcastManager.  
* Backend is based on MEAN stack.  
* Graph is plotted using D3.js by Querying MongoDB using Angular.js 

## To Do
* Improve UI/UX

## Changelog
* implemented network connection [2 hr]  
* implemented json parsing of sql data [2 hr]  
* implemented refreshing of list by a button [2 hr]  
* implemented list [1 hr]  
* implemented Alarm Manager [5 hr]  
* implemented sql [2 hr]  
