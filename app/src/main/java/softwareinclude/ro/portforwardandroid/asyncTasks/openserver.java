package softwareinclude.ro.portforwardandroid.asyncTasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.StringTokenizer;

import softwareinclude.ro.portforwardandroid.asyncTasks.NanoHTTPD.Response.IStatus;
import tab.list.FileContentProvider.UserSchema;


public class openserver extends NanoHTTPD {
    
	
	/**
     * Common mime type for dynamic content: binary
     */
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";

    /**
     * Default Index file names.
     */
    @SuppressWarnings("serial")
    public static final List<String> INDEX_FILE_NAMES = new ArrayList<String>() {

        {
            add("index.html");
            add("index.htm");
        }
    };
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    ContentResolver server_content;
    public void setContent(ContentResolver content){
    	server_content=content;
    }
    public ContentResolver getContent(){
    	return server_content;
    }
    @SuppressWarnings("serial")
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {

        {
            put("css", "text/css");
            put("htm", "text/html");
            put("html", "text/html");
            put("xml", "text/xml");
            put("java", "text/x-java-source, text/java");
            put("md", "text/plain");
            put("txt", "text/plain");
            put("asc", "text/plain");
            put("gif", "image/gif");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
            put("png", "image/png");
            put("svg", "image/svg+xml");
            put("mp3", "audio/mpeg");
            put("m3u", "audio/mpeg-url");
            put("mp4", "video/mp4");
            put("ogv", "video/ogg");
            put("flv", "video/x-flv");
            put("mov", "video/quicktime");
            put("swf", "application/x-shockwave-flash");
            put("js", "application/javascript");
            put("pdf", "application/pdf");
            put("doc", "application/msword");
            put("ogg", "application/x-ogg");
            put("zip", "application/octet-stream");
            put("exe", "application/octet-stream");
            put("class", "application/octet-stream");
            put("m3u8", "application/vnd.apple.mpegurl");
            put("ts", " video/mp2t");
        }
    };

    private static Map<String, WebServerPlugin> mimeTypeHandlers = new HashMap<String, WebServerPlugin>();

    
    private final boolean quiet;
    protected List<File> rootDirs;
    
    public openserver(String host, int port, File wwwroot, boolean quiet) {
        super(host, port);
        this.quiet = quiet;
        this.rootDirs = new ArrayList<File>();
        this.rootDirs.add(wwwroot);

        init();
    }
    public openserver(String host, int port, List<File> wwwroots, boolean quiet) {
        super(host, port);
        this.quiet = quiet;
        this.rootDirs = new ArrayList<File>(wwwroots);

        init();
    }
    /**
     * Used to initialize and customize the server.
     */
    public void init() {
    	
    }

	
	Handler handler ;
	static final int UPDATE_LIST_MES = 0;
	String msg = new String();
	
	
	String requestString=new String();
	NanoHTTPD server;
	public void go(String[] args) {

			// Defaults
	        int port = 8080;

	        String host = null; // bind to all interfaces by default
	        List<File> rootDirs = new ArrayList<File>();
	        boolean quiet = false;
	        Map<String, String> options = new HashMap<String, String>();

	        // Parse command-line, with short and long versions of the options.
	        for (int i = 0; i < args.length; ++i) {
	            if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--host")) {
	                host = args[i + 1];
	            } else if (args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("--port")) {
	                port = Integer.parseInt(args[i + 1]);
	            } else if (args[i].equalsIgnoreCase("-q") || args[i].equalsIgnoreCase("--quiet")) {
	                quiet = true;
	            } else if (args[i].equalsIgnoreCase("-d") || args[i].equalsIgnoreCase("--dir")) {
	                rootDirs.add(new File(args[i + 1]).getAbsoluteFile());
	            } else if (args[i].equalsIgnoreCase("--licence")) {
	               
	            } else if (args[i].startsWith("-X:")) {
	                int dot = args[i].indexOf('=');
	                if (dot > 0) {
	                    String name = args[i].substring(0, dot);
	                    String value = args[i].substring(dot + 1, args[i].length());
	                    options.put(name, value);
	                }
	            }
	        }
	        if (rootDirs.isEmpty()) {
	            rootDirs.add(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
	        }

	        options.put("host", host);
	        options.put("port", "" + port);
	        options.put("quiet", String.valueOf(quiet));
	        StringBuilder sb = new StringBuilder();
	        for (File dir : rootDirs) {
	            if (sb.length() > 0) {
	                sb.append(":");
	            }
	            try {
	                sb.append(dir.getCanonicalPath());
	            } catch (IOException ignored) {
	            }
	        }
	        options.put("home", sb.toString());
	        ServiceLoader<WebServerPluginInfo> serviceLoader = ServiceLoader.load(WebServerPluginInfo.class);

	        for (WebServerPluginInfo info : serviceLoader) {
	            String[] mimeTypes = info.getMimeTypes();
	            for (String mime : mimeTypes) {
	                String[] indexFiles = info.getIndexFilesForMimeType(mime);
	                if (!quiet) {
	                    System.out.print("# Found plugin for Mime type: \"" + mime + "\"");
	                    if (indexFiles != null) {
	                        System.out.print(" (serving index files: ");
	                        for (String indexFile : indexFiles) {
	                            System.out.print(indexFile + " ");
	                        }
	                    }
	                    System.out.println(").");
	                }
	                registerPluginForMimeType(indexFiles, mime, info.getWebServerPlugin(mime), options);
	            }
	        }
	        server =new openserver(host, port, rootDirs, quiet);
	        try {
	            server.start();
	            
	        } catch (IOException ioe) {
	            System.err.println("Couldn't start server:\n" + ioe);
	            System.exit(-1);
	        }
	        System.out.println("Server started, Hit Enter to stop.\n");
		 
	 }
    protected static void registerPluginForMimeType(String[] indexFiles, String mimeType, WebServerPlugin plugin, Map<String, String> commandLineOptions) {
        if (mimeType == null || plugin == null) {
            return;
        }

        if (indexFiles != null) {
            for (String filename : indexFiles) {
                int dot = filename.lastIndexOf('.');
                if (dot >= 0) {
                    String extension = filename.substring(dot + 1).toLowerCase();
                    openserver.MIME_TYPES.put(extension, mimeType);
                }
            }
            openserver.INDEX_FILE_NAMES.addAll(Arrays.asList(indexFiles));
        }
        openserver.mimeTypeHandlers.put(mimeType, plugin);
        plugin.initialize(commandLineOptions);
    }
    private boolean canServeUri(String uri, File homeDir) {
        boolean canServeUri;
        File f = new File(homeDir, uri);
        canServeUri = f.exists();
        if (!canServeUri) {
            String mimeTypeForFile = getMimeTypeForFile(uri);
            WebServerPlugin plugin = openserver.mimeTypeHandlers.get(mimeTypeForFile);
            if (plugin != null) {
                canServeUri = plugin.canServeUri(uri, homeDir);
            }
        }
        return canServeUri;
    }

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
     * instead of '+'.
     */
    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/")) {
                newUri += "/";
            } else if (tok.equals(" ")) {
                newUri += "%20";
            } else {
                try {
                    newUri += URLEncoder.encode(tok, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        }
        return newUri;
    }

    private String findIndexFileInDirectory(File directory) {
        for (String fileName : openserver.INDEX_FILE_NAMES) {
            File indexFile = new File(directory, fileName);
            if (indexFile.isFile()) {
                return fileName;
            }
        }
        return null;
    }

    protected Response getForbiddenResponse(String s) {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
    }

    protected Response getInternalErrorResponse(String s) {
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERROR: " + s);
    }
    // Get MIME type from file name extension, if possible
    private String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf('.');
        String mime = null;
        if (dot >= 0) {
            mime = openserver.MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? openserver.MIME_DEFAULT_BINARY : mime;
    }
    protected Response getNotFoundResponse() {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
    }
    protected String listDirectory(String uri, File f) {
        String heading = "Directory " + uri;
        StringBuilder msg =
                new StringBuilder("<html><head><title>" + heading + "</title><style><!--\n" + "span.dirname { font-weight: bold; }\n" + "span.filesize { font-size: 75%; }\n"
                        + "// -->\n" + "</style>" + "</head><body><h1>" + heading + "</h1>");

        String up = null;
        if (uri.length() > 1) {
            String u = uri.substring(0, uri.length() - 1);
            int slash = u.lastIndexOf('/');
            if (slash >= 0 && slash < u.length()) {
                up = uri.substring(0, slash + 1);
            }
        }

        List<String> files = Arrays.asList(f.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        }));
        Collections.sort(files);
        List<String> directories = Arrays.asList(f.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        }));
        Collections.sort(directories);
        if (up != null || directories.size() + files.size() > 0) {
            msg.append("<ul>");
            if (up != null || directories.size() > 0) {
                msg.append("<section class=\"directories\">");
                if (up != null) {
                    msg.append("<li><a rel=\"directory\" href=\"").append(up).append("\"><span class=\"dirname\">..</span></a></b></li>");
                }
                for (String directory : directories) {
                    String dir = directory + "/";
                    msg.append("<li><a rel=\"directory\" href=\"").append(encodeUri(uri + dir)).append("\"><span class=\"dirname\">").append(dir)
                            .append("</span></a></b></li>");
                }
                msg.append("</section>");
            }
            if (files.size() > 0) {
                msg.append("<section class=\"files\">");
                for (String file : files) {
                    msg.append("<li><a href=\"").append(encodeUri(uri + file)).append("\"><span class=\"filename\">").append(file).append("</span></a>");
                    File curFile = new File(f, file);
                    long len = curFile.length();
                    msg.append("&nbsp;<span class=\"filesize\">(");
                    if (len < 1024) {
                        msg.append(len).append(" bytes");
                    } else if (len < 1024 * 1024) {
                        msg.append(len / 1024).append(".").append(len % 1024 / 10 % 100).append(" KB");
                    } else {
                        msg.append(len / (1024 * 1024)).append(".").append(len % (1024 * 1024) / 10000 % 100).append(" MB");
                    }
                    msg.append(")</span></li>");
                }
                msg.append("</section>");
            }
            msg.append("</ul>");
        }
        msg.append("</body></html>");
        return msg.toString();
    }

    @Override
    public Response newFixedLengthResponse(IStatus status, String mimeType, String message) {
        Response response = super.newFixedLengthResponse(status, mimeType, message);
        response.addHeader("Accept-Ranges", "bytes");
        return response;
    }
    
    private Response respond(Map<String, String> headers, IHTTPSession session, String uri) {
        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        // Prohibit getting out of current directory
        if (uri.contains("../")) {
            return getForbiddenResponse("Won't serve ../ for security reasons.");
        }

        boolean canServeUri = false;
        File homeDir = null;
        for (int i = 0; !canServeUri && i < this.rootDirs.size(); i++) {
            homeDir = this.rootDirs.get(i);
            canServeUri = canServeUri(uri, homeDir);
        }
        if (!canServeUri) {
            return getNotFoundResponse();
        }

        // Browsers get confused without '/' after the directory, send a
        // redirect.
        File f = new File(homeDir, uri);
        if (f.isDirectory() && !uri.endsWith("/")) {
            uri += "/";
            Response res =
                    newFixedLengthResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML, "<html><body>Redirected: <a href=\"" + uri + "\">" + uri + "</a></body></html>");
            res.addHeader("Location", uri);
            return res;
        }

        if (f.isDirectory()) {
            // First look for index files (index.html, index.htm, etc) and if
            // none found, list the directory if readable.
            String indexFile = findIndexFileInDirectory(f);
            if (indexFile == null) {
                if (f.canRead()) {
                	if(uri.equalsIgnoreCase("/KM/")){
                		String path=new String();
                		ContentResolver conten =getContent();
                		Cursor check_finish_cursor = conten.query(Uri.parse("content://tab.list.d2d/temp_file"), new String[] { UserSchema._FILEPATH, UserSchema._MESSAGETOKEN }, "messagetoken='" +token + "'", null, null);
//        				Cursor check_finish_cursor = conten.query(Uri.parse("content://tab.list.d2d/temp_file"), new String[] { UserSchema._FILEPATH,UserSchema._MESSAGETOKEN }, null, null, null);
        			    if (check_finish_cursor.getCount() > 0) {
        				    check_finish_cursor.moveToFirst();

                        // 將未傳完的檔案，使用陣列儲存起來
//        				for (int i = 0; i < check_finish_cursor.getCount(); i++) {
//        					String token=check_finish_cursor.getString(1);
//        				    path = path+"&"+check_finish_cursor.getString(0);
//        				    check_finish_cursor.moveToNext();
//        				}
                            //2016/10/20只傳一塊出去
                            path = check_finish_cursor.getString(0);
                            check_finish_cursor.moveToNext();
        				    check_finish_cursor.close();
        				    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, "ret=0"+path);
        			    }
        			    else{
        			    	check_finish_cursor.close();
        			    	return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, "file not ready");
        			    }
        			    
        			    
                		 
                	}else{
                		// No index file, list the directory if it is readable
                        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, listDirectory(uri, f));
                	}
                    
                } else {
                    return getForbiddenResponse("No directory listing.");
                }
            } else {
                return respond(headers, session, uri + indexFile);
            }
        }

        String mimeTypeForFile = getMimeTypeForFile(uri);
        WebServerPlugin plugin = openserver.mimeTypeHandlers.get(mimeTypeForFile);
        Response response = null;
        if (plugin != null && plugin.canServeUri(uri, homeDir)) {
            response = plugin.serveFile(uri, headers, session, f, mimeTypeForFile);
            if (response != null && response instanceof InternalRewrite) {
                InternalRewrite rewrite = (InternalRewrite) response;
                return respond(rewrite.getHeaders(), session, rewrite.getUri());
            }
        } else {
            response = serveFile(uri, headers, f, mimeTypeForFile);
        }
        return response != null ? response : getNotFoundResponse();
    }
    String token=new String();
    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> header = session.getHeaders();
        Map<String, String> parms = session.getParms();
        String uri = session.getUri();

        if (!this.quiet) {
            System.out.println(session.getMethod() + " '" + uri + "' ");

            Iterator<String> e = header.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
            }
            e = parms.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                token=parms.get(value);
                System.out.println("  PRM: '" + value + "' = '" + parms.get(value) + "'");
            }
        }

        for (File homeDir : this.rootDirs) {
            // Make sure we won't die of an exception later
            if (!homeDir.isDirectory()) {
                return getInternalErrorResponse("given path is not a directory (" + homeDir + ").");
            }
        }
        return respond(Collections.unmodifiableMap(header), session, uri);
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI,
     * ignores all headers and HTTP parameters.
     */
    Response serveFile(String uri, Map<String, String> header, File file, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            String ifRange = header.get("if-range");
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

            String ifNoneMatch = header.get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && (ifNoneMatch.equals("*") || ifNoneMatch.equals(etag));

            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();

            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    FileInputStream fis = new FileInputStream(file);
                    fis.skip(startFrom);

                    res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, newLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + newLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes */" + fileLen);
                    res.addHeader("ETag", etag);
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified

                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    // supply the file
                    res = newFixedFileResponse(file, mime);
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = getForbiddenResponse("Reading file failed.");
        }

        return res;
    }

    private Response newFixedFileResponse(File file, String mime) throws FileNotFoundException {
        Response res;
        res = newFixedLengthResponse(Response.Status.OK, mime, new FileInputStream(file), (int) file.length());
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

}