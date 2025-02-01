import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Hashtable;
import java.util.Enumeration;


public class Filter {

    // alteratio counters
    public int capitals_replaced;
    public int links_replaced;

    public Filter()
    {
        this.capitals_replaced = 0;
        this.links_replaced = 0;
    }

    private static final Hashtable<String, String> citytable = new Hashtable<>();
    static {

        // Consistent (as required) hashtable to map the cities
        citytable.put("Sydney","Moscow");
        citytable.put("Melbourne","Saint-Petersburg");
        citytable.put("Brisbane","Sochi");
        citytable.put("Perth","Vladivostok");
        citytable.put("Adelaide","Kazan");
        citytable.put("Hobart","Kaliningrad");
        citytable.put("Canberra","Voronezsh");
        citytable.put("Darwin","Norilsk");
        citytable.put("Broome","Vologda");
        citytable.put("Albury-Wodonga","Omsk");
        citytable.put("Wollongong","Pskov");
        citytable.put("Newcastle","Adler");
        citytable.put("Townsville","Samara");
        citytable.put("Cairns","Ufa");
        citytable.put("Alice Springs","Krasnodar");
    }


    public String lineFilter(String line, String cityAU, String cityRU)
    {
        // match word Sydney in both lower or upper case, and without '/' infront of it because it indicates url
        Pattern pattern = Pattern.compile("[^/]" + cityAU, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        // while finding more matches go replace
        // while we have matched something to *Sydney
        while (matcher.find())
        {
            String match = matcher.group(); 
            // match = *Sydney (case insencetive)
            Pattern pat = Pattern.compile(cityAU, Pattern.CASE_INSENSITIVE); 
            // pat = Sydney (case insence) do new search inside match
            Matcher mat = pat.matcher(match);
            // mat = Sydney (from match)
            while (mat.find())
            // if found Sydney in *Sydney then
            {   
                String m = mat.group(); 
                // m = Sydney (the one we found)
                String patch = match.replace(m,cityRU);
                // replace m (Sydney) for cityRU (Moscow) inside match (*Sydney) 
                line = line.replace(match, patch);
                // replace match (*Sydney) for patch (*Moscow)  inside line .....*Sydeny....

                // count change
                this.capitals_replaced ++;
            }
        }
        return line;
    }  


    public String applyLineFilter(String line)
    {
        Enumeration citiesAu;
        
        
        citiesAu = citytable.keys();

        String cityAU;

        // replace cities
        while(citiesAu.hasMoreElements()) {
            cityAU = (String) citiesAu.nextElement();
            line = lineFilter(line, cityAU, citytable.get(cityAU));
        }

        // replace links
        line = urlFilter(line);
        return line;
    }

    public String urlFilter(String line)
    {
        // match word Sydney with both lower or upper case 's', and without '/' infront of it
        String re = "href=\"http://www.bom.gov.au";
        Pattern pattern = Pattern.compile(re, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);

        // while finding keep replacing
        while (matcher.find())
        {
            String match = matcher.group(); 
            String patch = "href=\"";
            line = line.replace(match, patch);
            this.links_replaced += 1; 

        }
        return line;
    }  
}
