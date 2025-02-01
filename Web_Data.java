// Custom data type to keep data from the web
import java.util.Hashtable;

public class Web_Data {

    // class attributes
    private Http_Header header;
    private byte[] payload_bytes;


    // constructor 1
    public Web_Data()
    {
        this.header          = null;
        this.payload_bytes   = null;
    }

    // constructor 2
    public Web_Data(String header_string, byte[] payload_bytes)
    {
        this.header    = new Http_Header(header_string);
        this.payload_bytes   = payload_bytes;

        if (this.header.has_content_text)
        {
            this.header.fix_length(payload_bytes.length);
        }
    }

    // constructor 3
    public Web_Data(Http_Header header, byte[] payload_bytes)
    {
        this.header     = header;
        this.payload_bytes    = payload_bytes;
        
        if (this.header.has_content_text)
        {
            this.header.fix_length(payload_bytes.length);
        }
    }

    public boolean is_content_text()
    {
        return this.header.has_content_text;
    }

    public byte[] get_payload()
    {
        return this.payload_bytes;
    }

    public Http_Header get_header()
    {
        return this.header;
    }


}


class Http_Header 
{
    // class attributes
    private String header_string;
    private Hashtable<String, String> headers_table;
    public boolean has_content_text;

    // constructor
    public Http_Header(String header_string)
    {
        this.header_string = header_string;
        this.headers_table = get_headers_table(header_string);
        this.has_content_text = has_content_text();
    }

    // change content length in header
    public void fix_length(int content_length)
    {
        this.headers_table.put("Content-Length", String.valueOf(content_length));
        String[] lines = this.header_string.split("\r\n");
        String new_header_string = "";

        for(String line : lines)
        {
            if(line.startsWith("Content-Length"))
            {
                line = "Content-Length: " + String.valueOf(content_length);
            }
            new_header_string = new_header_string + line + "\r\n";
        }
        this.header_string = new_header_string;
    }

    // tells if the content is textual
    private boolean has_content_text(){
        if(this.headers_table.containsKey("Content-Type"))
        {
            String con_type = this.headers_table.get("Content-Type");
            if (con_type.contains("text/html"))
            {
                return true;
            }
        }
        return false;
    }


    // place headers into hashmap keyed by the header names
    private Hashtable<String, String> get_headers_table(String headerString)
    {
        Hashtable<String, String> result = new Hashtable<>();
        String[] lines = headerString.split("\r\n");
        for(String line : lines)
        {
            String[] field_names = line.split(": ");
            String key;
            String val;
            if(field_names.length == 2)
            {
                key = field_names[0].trim();
                val = field_names[1].trim();
                result.put(key, val);
            }
        }
        return result;
    }


    @Override
    public String toString(){
        return header_string;
    }
}
