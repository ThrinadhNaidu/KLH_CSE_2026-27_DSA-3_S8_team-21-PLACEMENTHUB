import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class PlacementHubServer {
    static final Path DATA = Paths.get("data");
    static final Path STUDENTS = DATA.resolve("students.txt");
    static final Path ADMINS = DATA.resolve("admins.txt");
    static final Path COMPANIES = DATA.resolve("companies.txt");
    static final Path APPLICATIONS = DATA.resolve("applications.txt");
    static final Path NOTIFICATIONS = DATA.resolve("notifications.txt");
    static final Path DRIVES = DATA.resolve("drives.txt");
    static final Path HISTORY = DATA.resolve("placement_history.txt");

    static class Student {
        String id,name,email,password,branch; double cgpa;
        ArrayList<String> skills,training,internships,certifications,projects;
        String placementStatus;
        Student(String[] a){
            id=a[0]; name=a[1]; email=a[2]; password=a[3]; branch=a[4];
            cgpa=Double.parseDouble(a[5]); skills=list(a[6]); training=list(a[7]);
            internships=list(a[8]); certifications=list(a[9]); projects=list(a[10]);
            placementStatus=a.length>11?a[11]:"Not Placed";
        }
        static ArrayList<String> list(String s){return new ArrayList<>(Arrays.stream(s.split(",")).map(String::trim).filter(x->!x.isEmpty()).collect(Collectors.toList()));}
        String line(){return String.join("|", id,name,email,password,branch,String.valueOf(cgpa),join(skills),join(training),join(internships),join(certifications),join(projects),placementStatus);}
    }

    static class Company {
        String id,name,role,description; double minCGPA; ArrayList<String> requiredSkills;
        String requiredTraining,requiredInternship,location,pkg,deadline; int openings;
        Company(String[] a){
            id=a[0];name=a[1];role=a[2];description=a[3];minCGPA=Double.parseDouble(a[4]);
            requiredSkills=Student.list(a[5]);requiredTraining=a[6];requiredInternship=a[7];
            location=a[8];pkg=a[9];deadline=a[10];openings=a.length>11?Integer.parseInt(a[11]):0;
        }
        String line(){return String.join("|",id,name,role,description,String.valueOf(minCGPA),join(requiredSkills),requiredTraining,requiredInternship,location,pkg,deadline,String.valueOf(openings));}
    }

    static class Application {
        String id,studentId,studentName,companyId,companyName,role,score,date,status,remark;
        Application(String[] a){id=a[0];studentId=a[1];studentName=a[2];companyId=a[3];companyName=a[4];role=a[5];score=a[6];date=a[7];status=a[8];remark=a.length>9?a[9]:"";}
        String line(){return String.join("|",id,studentId,studentName,companyId,companyName,role,score,date,status,remark);}
    }
    static class Notification {
        String id,recipient,type,message,date,read;
        Notification(String[] a){id=a[0];recipient=a[1];type=a[2];message=a[3];date=a[4];read=a.length>5?a[5]:"false";}
        String line(){return String.join("|",id,recipient,type,message,date,read);}
    }
    static class Drive {
        String id,company,role,date,venue,openings,deadline,requirements;
        Drive(String[] a){id=a[0];company=a[1];role=a[2];date=a[3];venue=a[4];openings=a[5];deadline=a[6];requirements=a.length>7?a[7]:"";}
        String line(){return String.join("|",id,company,role,date,venue,openings,deadline,requirements);}
    }
    static class History {
        String studentId,studentName,company,role,pkg,date,status;
        History(String[] a){studentId=a[0];studentName=a[1];company=a[2];role=a[3];pkg=a[4];date=a[5];status=a[6];}
        String line(){return String.join("|",studentId,studentName,company,role,pkg,date,status);}
    }

    // Trie: prefix lookup for skill autocomplete without a library.
    static class Trie {
        static class Node { Map<Character,Node> next=new TreeMap<>(); boolean word; }
        Node root=new Node();
        void add(String value){Node n=root;for(char c:value.toLowerCase().toCharArray())n=n.next.computeIfAbsent(c,k->new Node());n.word=true;}
        void collect(Node n,String prefix,List<String> out){if(n.word)out.add(prefix);for(Map.Entry<Character,Node> e:n.next.entrySet())collect(e.getValue(),prefix+e.getKey(),out);}
        List<String> suggest(String prefix){Node n=root;String p=prefix.toLowerCase();for(char c:p.toCharArray()){n=n.next.get(c);if(n==null)return new ArrayList<>();}List<String> out=new ArrayList<>();collect(n,p,out);return out;}
    }

    static String join(List<String> x){return String.join(",",x);}
    static List<String> readLines(Path p)throws IOException{
        if(!Files.exists(p)) return new ArrayList<>();
        return Files.readAllLines(p,StandardCharsets.UTF_8);
    }
    static ArrayList<Student> students()throws IOException{
        ArrayList<Student> x=new ArrayList<>();
        for(String s:readLines(STUDENTS)) if(!s.isBlank()) x.add(new Student(s.split("\\|",-1)));
        return x;
    }
    static ArrayList<Company> companies()throws IOException{
        ArrayList<Company> x=new ArrayList<>();
        for(String s:readLines(COMPANIES)) if(!s.isBlank()) x.add(new Company(s.split("\\|",-1)));
        return x;
    }
    static void saveStudents(List<Student> x)throws IOException{Files.write(STUDENTS,x.stream().map(Student::line).collect(Collectors.toList()),StandardCharsets.UTF_8);}
    static void saveCompanies(List<Company> x)throws IOException{Files.write(COMPANIES,x.stream().map(Company::line).collect(Collectors.toList()),StandardCharsets.UTF_8);}
    static ArrayList<Application> applications()throws IOException{ArrayList<Application>x=new ArrayList<>();for(String s:readLines(APPLICATIONS))if(!s.isBlank())x.add(new Application(s.split("\\|",-1)));return x;}
    static ArrayList<Notification> notifications()throws IOException{ArrayList<Notification>x=new ArrayList<>();for(String s:readLines(NOTIFICATIONS))if(!s.isBlank())x.add(new Notification(s.split("\\|",-1)));return x;}
    static ArrayList<Drive> drives()throws IOException{ArrayList<Drive>x=new ArrayList<>();for(String s:readLines(DRIVES))if(!s.isBlank())x.add(new Drive(s.split("\\|",-1)));return x;}
    static ArrayList<History> history()throws IOException{ArrayList<History>x=new ArrayList<>();for(String s:readLines(HISTORY))if(!s.isBlank())x.add(new History(s.split("\\|",-1)));return x;}
    static void saveApplications(List<Application>x)throws IOException{Files.write(APPLICATIONS,x.stream().map(Application::line).collect(Collectors.toList()),StandardCharsets.UTF_8);}
    static void saveNotifications(List<Notification>x)throws IOException{Files.write(NOTIFICATIONS,x.stream().map(Notification::line).collect(Collectors.toList()),StandardCharsets.UTF_8);}
    static void saveDrives(List<Drive>x)throws IOException{Files.write(DRIVES,x.stream().map(Drive::line).collect(Collectors.toList()),StandardCharsets.UTF_8);}
    static void saveHistory(List<History>x)throws IOException{Files.write(HISTORY,x.stream().map(History::line).collect(Collectors.toList()),StandardCharsets.UTF_8);}
    static String now(){return java.time.LocalDate.now().toString();}
    static String value(Map<String,String> m,String key){return m.getOrDefault(key,"").trim();}
    static String nextId(String prefix,int size){return prefix+String.format("%03d",size+1);}

    // ================= DSA ALGORITHMS =================

    // 1. Linear Search
    static Student linearStudentSearch(List<Student> list,String id){
        for(Student s:list) if(s.id.equalsIgnoreCase(id)) return s;
        return null;
    }

    // 2. KMP Pattern Matching
    static int[] createLPS(String pattern){
        int[] lps=new int[pattern.length()]; int len=0,i=1;
        while(i<pattern.length()){
            if(pattern.charAt(i)==pattern.charAt(len)){lps[i++]=++len;}
            else if(len!=0) len=lps[len-1];
            else lps[i++]=0;
        }
        return lps;
    }
    static boolean kmpSearch(String text,String pattern){
        text=text.toLowerCase().trim(); pattern=pattern.toLowerCase().trim();
        if(pattern.isEmpty()) return false;
        int[] lps=createLPS(pattern); int i=0,j=0;
        while(i<text.length()){
            if(text.charAt(i)==pattern.charAt(j)){i++;j++;if(j==pattern.length())return true;}
            else if(j!=0) j=lps[j-1]; else i++;
        }
        return false;
    }

    // 3. Levenshtein Distance
    static int levenshtein(String a,String b){
        a=a.toLowerCase().trim();b=b.toLowerCase().trim();
        int n=a.length(),m=b.length();int[][] dp=new int[n+1][m+1];
        for(int i=0;i<=n;i++)dp[i][0]=i;for(int j=0;j<=m;j++)dp[0][j]=j;
        for(int i=1;i<=n;i++)for(int j=1;j<=m;j++){
            if(a.charAt(i-1)==b.charAt(j-1))dp[i][j]=dp[i-1][j-1];
            else dp[i][j]=1+Math.min(dp[i][j-1],Math.min(dp[i-1][j],dp[i-1][j-1]));
        }
        return dp[n][m];
    }

    // 4. Fuzzy matching
    static boolean fuzzyMatch(String a,String b){return levenshtein(a,b)<=2;}

    // 5. Jaccard Similarity
    static double jaccardSimilarity(List<String> required,List<String> student){
        HashSet<String> a=new HashSet<>(),b=new HashSet<>();
        for(String s:required)a.add(s.toLowerCase().trim());
        for(String s:student)b.add(s.toLowerCase().trim());
        HashSet<String> intersection=new HashSet<>(a);intersection.retainAll(b);
        HashSet<String> union=new HashSet<>(a);union.addAll(b);
        return union.isEmpty()?0:(double)intersection.size()/union.size();
    }

    static ArrayList<String> matchingSkills(List<String> student,List<String> required){
        ArrayList<String> matches=new ArrayList<>();
        for(String r:required) for(String s:student)
            if(s.equalsIgnoreCase(r)||kmpSearch(s,r)){matches.add(s);break;}
        return matches;
    }

    static boolean requirementMatches(List<String> values,String required){
        if(required==null||required.equalsIgnoreCase("none")||required.isBlank()) return true;
        for(String v:values) if(kmpSearch(v,required)||kmpSearch(required,v)) return true;
        return false;
    }

    static String jsonEscape(String s){
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ").replace("\r"," ");
    }
    static String q(String s){return "\""+jsonEscape(s==null?"":s)+"\"";}
    static String arr(List<String> x){return "["+x.stream().map(PlacementHubServer::q).collect(Collectors.joining(","))+"]";}

    static String studentJson(Student s){
        return "{\"id\":"+q(s.id)+",\"name\":"+q(s.name)+",\"email\":"+q(s.email)+",\"branch\":"+q(s.branch)+
        ",\"cgpa\":"+s.cgpa+",\"skills\":"+arr(s.skills)+",\"training\":"+arr(s.training)+
        ",\"internships\":"+arr(s.internships)+",\"certifications\":"+arr(s.certifications)+
        ",\"projects\":"+arr(s.projects)+",\"placementStatus\":"+q(s.placementStatus)+"}";
    }
    static String companyJson(Company c){
        return "{\"id\":"+q(c.id)+",\"name\":"+q(c.name)+",\"role\":"+q(c.role)+",\"description\":"+q(c.description)+
        ",\"minCGPA\":"+c.minCGPA+",\"requiredSkills\":"+arr(c.requiredSkills)+",\"requiredTraining\":"+q(c.requiredTraining)+
        ",\"requiredInternship\":"+q(c.requiredInternship)+",\"location\":"+q(c.location)+",\"pkg\":"+q(c.pkg)+",\"deadline\":"+q(c.deadline)+",\"openings\":"+c.openings+"}";
    }
    static String applicationJson(Application a){return "{\"id\":"+q(a.id)+",\"studentId\":"+q(a.studentId)+",\"studentName\":"+q(a.studentName)+",\"companyId\":"+q(a.companyId)+",\"companyName\":"+q(a.companyName)+",\"role\":"+q(a.role)+",\"score\":"+a.score+",\"date\":"+q(a.date)+",\"status\":"+q(a.status)+",\"remark\":"+q(a.remark)+"}";}
    static String notificationJson(Notification n){return "{\"id\":"+q(n.id)+",\"type\":"+q(n.type)+",\"message\":"+q(n.message)+",\"date\":"+q(n.date)+",\"read\":"+n.read+"}";}
    static String driveJson(Drive d){return "{\"id\":"+q(d.id)+",\"company\":"+q(d.company)+",\"role\":"+q(d.role)+",\"date\":"+q(d.date)+",\"venue\":"+q(d.venue)+",\"openings\":"+q(d.openings)+",\"deadline\":"+q(d.deadline)+",\"requirements\":"+q(d.requirements)+"}";}
    static String historyJson(History h){return "{\"studentId\":"+q(h.studentId)+",\"studentName\":"+q(h.studentName)+",\"company\":"+q(h.company)+",\"role\":"+q(h.role)+",\"pkg\":"+q(h.pkg)+",\"date\":"+q(h.date)+",\"status\":"+q(h.status)+"}";}

    static double cgpaScore(Student s,Company c){return c.minCGPA>=10?Math.min(100,s.cgpa*10):Math.min(100,Math.max(0,(s.cgpa/10)*100));}
    static double experienceScore(Student s,Company c){
        boolean training=requirementMatches(s.training,c.requiredTraining), internship=requirementMatches(s.internships,c.requiredInternship);
        return (training?50:0)+(internship?50:0);
    }
    static double compositeScore(Student s,Company c){
        return Math.min(100,0.5*jaccardSimilarity(c.requiredSkills,s.skills)*100+0.3*cgpaScore(s,c)+0.2*experienceScore(s,c));
    }
    static Trie skillTrie()throws IOException{
        Trie trie=new Trie();for(Student s:students())for(String skill:s.skills)trie.add(skill);for(Company c:companies())for(String skill:c.requiredSkills)trie.add(skill);return trie;
    }

    static String body(HttpExchange e)throws IOException{return new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);}
    static Map<String,String> parseJson(String s){
        Map<String,String> m=new HashMap<>();
        java.util.regex.Matcher z=java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}\\n]+))").matcher(s);
        while(z.find())m.put(z.group(1),z.group(2)!=null?z.group(2):z.group(3).trim());
        return m;
    }
    static Map<String,String> query(HttpExchange e){
        Map<String,String> m=new HashMap<>();String x=e.getRequestURI().getRawQuery();
        if(x==null)return m;for(String p:x.split("&")){String[] a=p.split("=",2);if(a.length==2)m.put(URLDecoder.decode(a[0],StandardCharsets.UTF_8),URLDecoder.decode(a[1],StandardCharsets.UTF_8));}
        return m;
    }
    static void send(HttpExchange e,String content)throws IOException{
        byte[] b=content.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json; charset=UTF-8");
        e.sendResponseHeaders(200,b.length);try(OutputStream o=e.getResponseBody()){o.write(b);}
    }
    static void sendFile(HttpExchange e,Path p)throws IOException{
        if(!Files.exists(p)){e.sendResponseHeaders(404,0);e.close();return;}
        String ct=p.toString().endsWith(".html")?"text/html":p.toString().endsWith(".css")?"text/css":"application/javascript";
        e.getResponseHeaders().set("Content-Type",ct+"; charset=UTF-8");byte[] b=Files.readAllBytes(p);e.sendResponseHeaders(200,b.length);try(OutputStream o=e.getResponseBody()){o.write(b);}
    }

    static void handleApi(HttpExchange e)throws Exception{
        String path=e.getRequestURI().getPath();Map<String,String> qp=query(e);

        if(path.equals("/api/login")){
            Map<String,String> m=parseJson(body(e));String role=m.get("role"),email=m.get("email"),pass=m.get("password");
            if("admin".equals(role)){
                for(String l:readLines(ADMINS)){String[] a=l.split("\\|",-1);if(a.length>=4&&a[2].equalsIgnoreCase(email)&&a[3].equals(pass)){send(e,"{\"success\":true,\"user\":{\"role\":\"admin\",\"id\":"+q(a[0])+",\"name\":"+q(a[1])+"}}");return;}}
            }else{
                for(Student s:students())if(s.email.equalsIgnoreCase(email)&&s.password.equals(pass)){send(e,"{\"success\":true,\"user\":{\"role\":\"student\",\"id\":"+q(s.id)+",\"name\":"+q(s.name)+"}}");return;}
            }
            send(e,"{\"success\":false,\"message\":\"Invalid login details.\"}");return;
        }

        if(path.equals("/api/register")){
            Map<String,String> m=parseJson(body(e));ArrayList<Student> ss=students();
            double newCgpa;try{newCgpa=Double.parseDouble(value(m,"cgpa"));}catch(Exception ex){send(e,"{\"success\":false,\"message\":\"CGPA must be a number between 0 and 10.\"}");return;}
            if(value(m,"id").isBlank()||value(m,"name").isBlank()||value(m,"email").isBlank()||value(m,"password").isBlank()||value(m,"branch").isBlank()||newCgpa<0||newCgpa>10){send(e,"{\"success\":false,\"message\":\"Please complete all required fields and enter a valid CGPA.\"}");return;}
            if(linearStudentSearch(ss,m.get("id"))!=null||ss.stream().anyMatch(s->s.email.equalsIgnoreCase(m.get("email")))){send(e,"{\"success\":false,\"message\":\"Student ID or email already exists.\"}");return;}
            Student s=new Student(new String[]{m.get("id"),m.get("name"),m.get("email"),m.get("password"),m.get("branch"),m.get("cgpa"),
                m.getOrDefault("skills",""),m.getOrDefault("training",""),m.getOrDefault("internships",""),m.getOrDefault("certifications",""),m.getOrDefault("projects",""),"Not Placed"});
            ss.add(s);saveStudents(ss);send(e,"{\"success\":true,\"message\":\"Registration successful. Please login.\"}");return;
        }

        if(path.equals("/api/student")){
            Student s=linearStudentSearch(students(),qp.get("id"));send(e,s==null?"{\"student\":null}":"{\"student\":"+studentJson(s)+"}");return;
        }

        if(path.equals("/api/student/update")){
            Map<String,String> m=parseJson(body(e));ArrayList<Student> ss=students();Student s=linearStudentSearch(ss,m.get("id"));
            if(s==null){send(e,"{\"success\":false,\"message\":\"Student not found.\"}");return;}
            s.name=m.getOrDefault("name",s.name);s.branch=m.getOrDefault("branch",s.branch);s.cgpa=Double.parseDouble(m.getOrDefault("cgpa",String.valueOf(s.cgpa)));
            s.skills=Student.list(m.getOrDefault("skills",""));s.training=Student.list(m.getOrDefault("training",""));s.internships=Student.list(m.getOrDefault("internships",""));
            s.certifications=Student.list(m.getOrDefault("certifications",""));s.projects=Student.list(m.getOrDefault("projects",""));saveStudents(ss);
            send(e,"{\"success\":true,\"message\":\"Profile updated successfully.\"}");return;
        }

        if(path.equals("/api/companies")){
            String q=qp.getOrDefault("q","");ArrayList<Company> cs=companies();List<Company> out=new ArrayList<>();
            for(Company c:cs) if(q.isBlank()||kmpSearch(c.name,q)||kmpSearch(c.role,q)||c.requiredSkills.stream().anyMatch(x->kmpSearch(x,q)))out.add(c);
            send(e,"{\"companies\":["+out.stream().map(PlacementHubServer::companyJson).collect(Collectors.joining(","))+"]}");return;
        }

        if(path.equals("/api/company/add")){
            Map<String,String> m=parseJson(body(e));ArrayList<Company> cs=companies();String id="COMP"+String.format("%03d",cs.size()+1);
            Company c=new Company(new String[]{id,m.get("name"),m.get("role"),m.getOrDefault("description",""),m.get("minCGPA"),m.getOrDefault("requiredSkills",""),
                m.getOrDefault("requiredTraining","None"),m.getOrDefault("requiredInternship","None"),m.getOrDefault("location",""),m.getOrDefault("pkg",""),m.getOrDefault("deadline","")});
            cs.add(c);saveCompanies(cs);send(e,"{\"success\":true,\"message\":\"Company added successfully.\"}");return;
        }

        if(path.equals("/api/company/delete")){
            ArrayList<Company> cs=companies();boolean ok=cs.removeIf(c->c.id.equalsIgnoreCase(qp.get("id")));saveCompanies(cs);
            send(e,"{\"success\":"+ok+",\"message\":\""+(ok?"Company deleted successfully.":"Company not found.")+"\"}");return;
        }

        if(path.equals("/api/students")){
            String q=qp.getOrDefault("q","");double min=qp.getOrDefault("minCGPA","").isBlank()?0:Double.parseDouble(qp.get("minCGPA"));
            List<Student> out=new ArrayList<>();for(Student s:students())if(s.cgpa>=min&&(q.isBlank()||kmpSearch(s.id,q)||kmpSearch(s.name,q)||s.skills.stream().anyMatch(x->kmpSearch(x,q))))out.add(s);
            send(e,"{\"students\":["+out.stream().map(PlacementHubServer::studentJson).collect(Collectors.joining(","))+"]}");return;
        }

        if(path.equals("/api/fuzzy")){
            String q=qp.getOrDefault("q","");StringBuilder out=new StringBuilder();
            for(Student s:students())for(String skill:s.skills)if(!q.isBlank()&&fuzzyMatch(q,skill))
                out.append("{\"name\":").append(q(s.name)).append(",\"id\":").append(q(s.id)).append(",\"skill\":").append(q(skill)).append(",\"distance\":").append(levenshtein(q,skill)).append("},");
            if(out.length()>0)out.setLength(out.length()-1);send(e,"{\"results\":["+out+"]}");return;
        }

        if(path.equals("/api/skills/autocomplete")){
            List<String> suggestions=skillTrie().suggest(qp.getOrDefault("q",""));
            send(e,"{\"suggestions\":"+arr(suggestions.subList(0,Math.min(10,suggestions.size())))+"}");return;
        }

        if(path.equals("/api/application/apply")){
            Map<String,String> m=parseJson(body(e));ArrayList<Application> apps=applications();
            Student s=linearStudentSearch(students(),value(m,"studentId"));
            Company c=companies().stream().filter(x->x.id.equalsIgnoreCase(value(m,"companyId"))).findFirst().orElse(null);
            if(s==null||c==null){send(e,"{\"success\":false,\"message\":\"Student or company not found.\"}");return;}
            if(apps.stream().anyMatch(a->a.studentId.equalsIgnoreCase(s.id)&&a.companyId.equalsIgnoreCase(c.id))){send(e,"{\"success\":false,\"message\":\"You have already applied for this company.\"}");return;}
            ArrayList<String> reasons=new ArrayList<>();if(s.cgpa<c.minCGPA)reasons.add("CGPA below requirement");if(!requirementMatches(s.training,c.requiredTraining))reasons.add("Required training missing");if(!requirementMatches(s.internships,c.requiredInternship))reasons.add("Required internship missing");
            if(!reasons.isEmpty()){send(e,"{\"success\":false,\"message\":\"Not eligible: "+jsonEscape(String.join(", ",reasons))+"\"}");return;}
            double score=compositeScore(s,c);Application a=new Application(new String[]{nextId("APP",apps.size()+1),s.id,s.name,c.id,c.name,c.role,String.format(Locale.US,"%.2f",score),now(),"PENDING",""});apps.add(a);saveApplications(apps);
            ArrayList<Notification> ns=notifications();ns.add(new Notification(new String[]{nextId("NOT",ns.size()+1),"admin","APPLICATION","New application from "+s.name+" for "+c.name+" - "+c.role,now(),"false"}));saveNotifications(ns);
            send(e,"{\"success\":true,\"application\":"+applicationJson(a)+"}");return;
        }

        if(path.equals("/api/applications")){
            String student=qp.getOrDefault("studentId","");String status=qp.getOrDefault("status","");List<Application> out=applications().stream().filter(a->(student.isBlank()||a.studentId.equalsIgnoreCase(student))&&(status.isBlank()||a.status.equalsIgnoreCase(status))).collect(Collectors.toList());
            send(e,"{\"applications\":["+out.stream().map(PlacementHubServer::applicationJson).collect(Collectors.joining(","))+"]}");return;
        }

        if(path.equals("/api/application/status")||path.equals("/api/application/approve")||path.equals("/api/application/reject")){
            Map<String,String> m=parseJson(body(e));ArrayList<Application> apps=applications();Application a=apps.stream().filter(x->x.id.equalsIgnoreCase(value(m,"applicationId"))).findFirst().orElse(null);String status=path.endsWith("approve")?"APPROVED":path.endsWith("reject")?"REJECTED":value(m,"status");
            if(a==null||!Arrays.asList("APPROVED","REJECTED","SHORTLISTED","INTERVIEW","SELECTED","NOT_SELECTED").contains(status)){send(e,"{\"success\":false,\"message\":\"Invalid application or status.\"}");return;}
            a.status=status;a.remark=value(m,"remark");saveApplications(apps);ArrayList<Notification> ns=notifications();ns.add(new Notification(new String[]{nextId("NOT",ns.size()+1),a.studentId,"APPLICATION", "Your "+a.companyName+" application is now "+status+(a.remark.isBlank()?"":". "+a.remark),now(),"false"}));saveNotifications(ns);
            if("SELECTED".equals(status)){ArrayList<History> hs=history();hs.add(new History(new String[]{a.studentId,a.studentName,a.companyName,a.role,"",now(),"SELECTED"}));saveHistory(hs);}
            send(e,"{\"success\":true,\"message\":\"Application status updated.\"}");return;
        }

        if(path.equals("/api/notifications")){
            String recipient=qp.getOrDefault("recipient","");List<Notification> out=notifications().stream().filter(n->recipient.isBlank()||n.recipient.equalsIgnoreCase(recipient)).collect(Collectors.toList());
            send(e,"{\"notifications\":["+out.stream().map(PlacementHubServer::notificationJson).collect(Collectors.joining(","))+"]}");return;
        }

        if(path.equals("/api/notifications/read")){
            Map<String,String> m=parseJson(body(e));ArrayList<Notification> ns=notifications();for(Notification n:ns)if(n.id.equalsIgnoreCase(value(m,"id")))n.read="true";saveNotifications(ns);send(e,"{\"success\":true}");return;
        }

        if(path.equals("/api/notifications/send")){
            Map<String,String> m=parseJson(body(e));String recipient=value(m,"recipient"),message=value(m,"message");
            if(recipient.isBlank()||message.isBlank()){send(e,"{\"success\":false,\"message\":\"Recipient and message are required.\"}");return;}
            if(linearStudentSearch(students(),recipient)==null){send(e,"{\"success\":false,\"message\":\"Student not found.\"}");return;}
            ArrayList<Notification> ns=notifications();Notification n=new Notification(new String[]{nextId("NOT",ns.size()+1),recipient,"ADMIN_MESSAGE",message,now(),"false"});ns.add(n);saveNotifications(ns);
            send(e,"{\"success\":true,\"message\":\"Message sent to the student.\"}");return;
        }

        if(path.equals("/api/drives")){send(e,"{\"drives\":["+drives().stream().map(PlacementHubServer::driveJson).collect(Collectors.joining(","))+"]}");return;}
        if(path.equals("/api/drive/add")){
            Map<String,String> m=parseJson(body(e));ArrayList<Drive> ds=drives();Drive d=new Drive(new String[]{nextId("DRV",ds.size()+1),value(m,"company"),value(m,"role"),value(m,"date"),value(m,"venue"),value(m,"openings"),value(m,"deadline"),value(m,"requirements")});ds.add(d);saveDrives(ds);send(e,"{\"success\":true,\"drive\":"+driveJson(d)+"}");return;
        }
        if(path.equals("/api/placement-history")){String id=qp.getOrDefault("studentId","");List<History> hs=history().stream().filter(h->id.isBlank()||h.studentId.equalsIgnoreCase(id)).collect(Collectors.toList());send(e,"{\"history\":["+hs.stream().map(PlacementHubServer::historyJson).collect(Collectors.joining(","))+"]}");return;}

        if(path.equals("/api/analytics")){int pending=0,approved=0,selected=0;for(Application a:applications()){if(a.status.equals("PENDING"))pending++;if(a.status.equals("APPROVED"))approved++;if(a.status.equals("SELECTED"))selected++;}send(e,"{\"students\":"+students().size()+",\"companies\":"+companies().size()+",\"applications\":"+applications().size()+",\"pending\":"+pending+",\"approved\":"+approved+",\"selected\":"+selected+"}");return;}

        if(path.equals("/api/stats")){int pending=0,approved=0,selected=0;for(Application a:applications()){if(a.status.equals("PENDING"))pending++;if(a.status.equals("APPROVED"))approved++;if(a.status.equals("SELECTED"))selected++;}send(e,"{\"students\":"+students().size()+",\"companies\":"+companies().size()+",\"applications\":"+applications().size()+",\"pending\":"+pending+",\"approved\":"+approved+",\"selected\":"+selected+"}");return;}

        if(path.equals("/api/match")){
            Company c=companies().stream().filter(x->x.id.equalsIgnoreCase(qp.get("company"))).findFirst().orElse(null);Student s=linearStudentSearch(students(),qp.get("student"));
            if(c==null||s==null){send(e,"{\"eligible\":false,\"reasons\":[\"Record not found\"]}");return;}
            ArrayList<String> reasons=new ArrayList<>();if(s.cgpa<c.minCGPA)reasons.add("CGPA below requirement");
            if(!requirementMatches(s.training,c.requiredTraining))reasons.add("Required training missing");
            if(!requirementMatches(s.internships,c.requiredInternship))reasons.add("Required internship missing");
            ArrayList<String> ms=matchingSkills(s.skills,c.requiredSkills);double skill=jaccardSimilarity(c.requiredSkills,s.skills)*100;double cgpa=cgpaScore(s,c);double experience=experienceScore(s,c);double overall=compositeScore(s,c);
            send(e,"{\"eligible\":"+(reasons.isEmpty())+",\"similarity\":"+String.format(Locale.US,"%.2f",skill)+",\"skillMatch\":"+String.format(Locale.US,"%.2f",skill)+",\"cgpaScore\":"+String.format(Locale.US,"%.2f",cgpa)+",\"experienceScore\":"+String.format(Locale.US,"%.2f",experience)+",\"overallScore\":"+String.format(Locale.US,"%.2f",overall)+",\"matchedSkills\":"+arr(ms)+",\"reasons\":"+arr(reasons)+"}");return;
        }

        if(path.equals("/api/match-all")){
            Company c=companies().stream().filter(x->x.id.equalsIgnoreCase(qp.get("company"))).findFirst().orElse(null);
            if(c==null){send(e,"{\"results\":[]}");return;}
            // Priority Queue / Max Heap: keep the highest scoring candidates at the head.
            class Score{Student s;double score;ArrayList<String> ms;Score(Student s,double x,ArrayList<String>m){this.s=s;score=x;ms=m;}}
            PriorityQueue<Score> heap=new PriorityQueue<>((a,b)->Double.compare(b.score,a.score));
            for(Student s:students())if(s.cgpa>=c.minCGPA&&requirementMatches(s.training,c.requiredTraining)&&requirementMatches(s.internships,c.requiredInternship))
                heap.add(new Score(s,compositeScore(s,c),matchingSkills(s.skills,c.requiredSkills)));
            int limit=qp.getOrDefault("limit","20").equalsIgnoreCase("all")?Integer.MAX_VALUE:Integer.parseInt(qp.getOrDefault("limit","20"));StringBuilder out=new StringBuilder();
            for(int rank=0;rank<limit&&!heap.isEmpty();rank++){Score x=heap.poll();out.append("{\"name\":").append(q(x.s.name)).append(",\"id\":").append(q(x.s.id)).append(",\"branch\":").append(q(x.s.branch))
                .append(",\"cgpa\":").append(x.s.cgpa).append(",\"similarity\":").append(String.format(Locale.US,"%.2f",x.score*100)).append(",\"overallScore\":").append(String.format(Locale.US,"%.2f",x.score*100)).append(",\"experienceScore\":").append(String.format(Locale.US,"%.2f",experienceScore(x.s,c))).append(",\"matchedSkills\":").append(arr(x.ms)).append("},");}
            if(out.length()>0)out.setLength(out.length()-1);send(e,"{\"results\":["+out+"]}");return;
        }

        send(e,"{\"message\":\"API endpoint not found\"}");
    }

    public static void main(String[] args)throws Exception{
        Files.createDirectories(DATA);
        for(Path file:Arrays.asList(APPLICATIONS,NOTIFICATIONS,DRIVES,HISTORY))if(!Files.exists(file))Files.createFile(file);
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server=HttpServer.create(new InetSocketAddress("0.0.0.0", port),0);
        server.createContext("/api/",e->{try{handleApi(e);}catch(Exception ex){ex.printStackTrace();try{send(e,"{\"success\":false,\"message\":\"Server error: "+jsonEscape(ex.getMessage()==null?"":ex.getMessage())+"\"}");}catch(Exception ignored){}}});
        server.createContext("/",e->{try{
            String p=e.getRequestURI().getPath();if(p.equals("/"))p="/index.html";
            if(p.contains("..")){e.sendResponseHeaders(403,0);e.close();return;}
            sendFile(e,Paths.get("web"+p));
        }catch(Exception ex){try{e.sendResponseHeaders(404,0);e.close();}catch(Exception ignored){}}});
        server.start();
        System.out.println("Placement Hub running on port "+port);
    }
}
