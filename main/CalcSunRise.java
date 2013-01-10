/*
 * CalcSunRise.java
 *
 * Created on February 25, 2002, 11:01 AM
 */

/**
 *
 * @author  TWu
 * @version 
 */
public class CalcSunRise {

    /** Creates new CalcSunRise */
    public CalcSunRise() {
    }

}

<%
   ':::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   ':::                                                                         :::
   ':::  These functions calculate sunrise and sunset times for any given       :::
   ':::  latitude and longitude. They may also be used to calculate such        :::
   ':::  things as astronomical twilight, nautical twilight and civil           :::
   ':::  twilight.                                                              :::
   ':::                                                                         :::
   ':::  SPECIAL NOTES: This code is valid for dates from 1901 to 2099, and     :::
   ':::                 will not calculate sunrise/set times for latitudes      :::
   ':::                 above/below 63/-63 degrees.                             :::
   ':::                                                                         :::
   ':::  This code is based on the work of several others, including Jean       :::
   ':::  Meeus, Todd Guillory,  Christophe David, Kieth Burnett and Roger W.    :::
   ':::  Sinnott (credit where due!)                                            :::
   ':::                                                                         :::
   ':::  Converted to VBScript and cleaned-up by Mike Shaffer.                  :::
   ':::                                                                         :::
   ':::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

   static final pi = Math.acos(-1)
    
   double degrees = 180 / pi;
   double radians = pi / 180;

   ':::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   ':::   Returns an angle in range of 0 to (2 * pi)                            :::
   ':::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   public double GetRange (double x) {

      double temp1
      double temp2

      temp1 = x / (2 * pi);
      temp2 = (2 * pi) * (temp1 - fix(temp1));
      if (temp2 < 0)
         temp2 = (2 * pi) + temp2;
      
      return temp2

   }


   ':::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   ':::   Returns 24 hour time from decimal time                                :::
   ':::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   Function GetMilitaryTime(DecimalTime, GMTOffset)
     
      dim temp1
      dim temp2

      ' Handle 24-hour time wrap
      if decimaltime+gmtoffset < 0 then decimaltime = decimaltime + 24
      if decimaltime+gmtoffset > 24 then decimaltime = decimaltime - 24

      temp1 = ABS(DecimalTime + GMTOffset)
      temp2 = INT(temp1)
      temp1 = 60 * (temp1 - temp2)
      temp1 = right("0000" & CSTR(INT(temp2 * 100 + temp1 + .5)), 4)

      GetMilitaryTime = left(temp1, 2) & ":" & right(temp1, 2)

   END Function


   ':::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   ':::   This routine does all the real work                                   :::
   ':::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   function GetSunRiseSet(latitude, ByVal longitude, ZoneRelativeGMT, RiseOrSet, Year, Month, Day)
      if abs(latitude) > 63 then
         GetSunRiseSet = "{invalid latitude}"
         Exit Function
      end if      

      ' An altitude of -0.833 is generally accepted as the angle of
      ' the sun at which sunrise/sunset occurs. It is not exactly
      ' zero because of refraction effects of the earth's atmosphere.
      Altitude = -0.833

      select case ucase(RiseOrSet)
         case "S"
            RS = -1
         case else
            RS = 1
      end select

      Ephem2000Day = 367 * y - 7 * (y + (m + 9) \ 12) \ 4 + 275 * m \ 9 + d - 730531.5

      utold = pi
      utnew = 0
      sinalt = cdbl(SIN(altitude * radians))	' solar altitude
      sinphi = cdbl(SIN(latitude * radians))	' viewer's latitude
      cosphi = cdbl(COS(latitude * radians))	'
      longitude = cdbl(longitude * radians)	' viewer's longitude

      Err.Clear
      On Error Resume Next

      DO WHILE (ABS(utold - utnew) > .001) and (ct < 35)

        ct = ct + 1

        utold = utnew
        days = Ephem2000Day + utold / (2 * pi)
        t = days / 36525
        '  These 'magic' numbers are orbital elements of the sun, and should not be changed
        L = GetRange(4.8949504201433 + 628.331969753199 * t)
        G = GetRange(6.2400408 + 628.3019501 * t)
        ec = .033423 * SIN(G) + .00034907 * SIN(2. * G)
        lambda = L + ec
        E = -1 * ec + .0430398 * SIN(2. * lambda) - .00092502 * SIN(4. * lambda)
        obl = .409093 - .0002269 * t

        ' Obtain ASIN of (SIN(obl) * SIN(lambda))
        delta = SIN(obl) * SIN(lambda)
        delta = ATN(delta / (sqr(1 - delta * delta)))

        GHA = utold - pi + E
        cosc = (sinalt - sinphi * SIN(delta)) / (cosphi * COS(delta))
        SELECT CASE cosc
        CASE cosc > 1
          correction = 0
        CASE cosc < -1
          correction = pi
        CASE ELSE
          correction = atn((sqr(1 - cosc * cosc)) / cosc)
        END SELECT

        utnew = GetRange(utold - (GHA + longitude + RS * correction))

      LOOP

      If err = 0 then
         GetSunRiseSet = GetMilitaryTime(utnew * degrees / 15, ZoneRelativeGMT)
      else
         GetSunRiseSet = "{err}"
      end if

   end function





   ':::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   ':::  Here is test harness. This code should be removed for production.      :::
   ':::                                                                         :::
   ':::  This example code shows today's sunrise time for Dallas                :::
   ':::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

   y = year(now)
   m = month(now)
   d = day(now)

   ' Set these to the latitude/longitude of the location
   MyLatitude = 32.9697
   MyLongitude = -96.80322

   ' Set this to your offset from GMT (e.g. for Dallas is -6)
   ' NOTE: The routine does NOT handle switches to/from daylight savings 
   '       time, so beware!
   MyTimeZone = -6

   ' Note:Set RiseOrSet to "R" for sunrise, "S" for sunset
   RiseOrSet = "R"
   ttt = GetSunRiseSet(MyLatitude, MyLongitude, MyTimeZone, RiseOrSet, y, m, d)

   RiseOrSet = "S"
   sss = GetSunRiseSet(MyLatitude, MyLongitude, MyTimeZone, RiseOrSet, y, m, d)

   response.write "<html><body>Sunrise for Dallas on " & _
                  datevalue(now) & " occurs at: " & ttt & _
                  " (24-hour time)<P>Sunset occurs at " & _
                  sss & "</body></html>"

%>



























'********************************************************************
'   Sun rise/set /twilight
'   From Explanatory Supplement
'   definitions and functions
DEFDBL A-Z
pi = 4 * ATN(1)
tpi = 2 * pi
twopi = tpi
degs = 180 / pi
rads = pi / 180
'
'   the function below returns the true integer part,
'   even for negative numbers
'
DEF FNipart (x) = SGN(x) * INT(ABS(x))
'
'   FNday only works between 1901 to 2099 - see Meeus chapter 7
'
DEF FNday (y, m, d, h) = 367 * y - 7 * (y + (m + 9) \ 12) \ 4 + 275 * m \ 9 + d - 730531.5 + h / 24
'
'   define some arc cos and arc sin functions
'
DEF FNacos (x)
    s = SQR(1 - x * x)
    FNacos = ATN(s / x)
END DEF
DEF FNasin (x)
    c = SQR(1 - x * x)
    FNasin = ATN(x / c)
END DEF
'
'   the function below returns an angle in the range
'   0 to two pi
'
DEF FNrange (x)
    b = x / tpi
    a = tpi * (b - FNipart(b))
    IF a < 0 THEN a = tpi + a
    FNrange = a
END DEF
'
'       the function below returns the time in 24 hour
'   notation - hhmm - given a decimal hours figure.
DEF FNdegmin$ (d)
c = ABS(d)
a = INT(c)
b = 60 * (c - a)
d$ = STR$(INT(a * 100 + b + .5))
d$ = RIGHT$(d$, LEN(d$) - 1)
WHILE LEN(d$) < 4
    d$ = "0" + d$
WEND
FNdegmin$ = d$
END DEF
CLS
'
'    get the date and geographical position from user
'
INPUT "     lat : ", glat
INPUT "    long : ", glong
INPUT "    zone : ", zone
INPUT "rise/set : ", riset
INPUT " Sun alt : ", altitude
INPUT "   year  : ", y
INPUT "   month : ", m
INPUT "   day   : ", d
day = FNday(y, m, d, 0)
PRINT "  day no : "; day
utold = pi
utnew = 0
sinalt = SIN(altitude * rads) 'go for the sunrise/sunset altitude first
sinphi = SIN(glat * rads)
cosphi = COS(glat * rads)
glong = glong * rads
DO WHILE ABS(utold - utnew) > .001
  utold = utnew
  days = day + utold / tpi
  t = days / 36525
  '     get arguments of Sun's orbit
  L = FNrange(4.8949504201433# + 628.331969753199# * t)
  G = FNrange(6.2400408# + 628.3019501# * t)
  ec = .033423 * SIN(G) + .00034907# * SIN(2 * G)
  lambda = L + ec
  E = -1 * ec + .0430398# * SIN(2 * lambda) - .00092502# * SIN(4 * lambda)
  obl = .409093 - .0002269# * t
  delta = FNasin(SIN(obl) * SIN(lambda))
  GHA = utold - pi + E
  cosc = (sinalt - sinphi * SIN(delta)) / (cosphi * COS(delta))
  SELECT CASE cosc
  CASE cosc > 1
    correction = 0
  CASE cosc < -1
    correction = pi
  CASE ELSE
    correction = FNacos(cosc)
  END SELECT
  utnew = FNrange(utold - (GHA + glong + riset * correction))
LOOP
PRINT "    UT   : "; FNdegmin$(utnew * degs / 15)
PRINT "   zone  : "; FNdegmin$(utnew * degs / 15 + zone)
END
'******************************************************************
