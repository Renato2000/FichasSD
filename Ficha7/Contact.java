import java.util.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class Contact {
    private String name;
    private int age;
    private long phoneNumber;
    private String company;     // Pode ser null
    private List<String> emails;

    public Contact (String name, int age, long phone_number, String company, List<String> emails) {
        this.name = name;
        this.age = age;
        this.phoneNumber = phone_number;
        this.company = company;
        this.emails = new ArrayList<>(emails);
    }

    public void serialize(DataOutputStream out) throws IOException{
        out.writeUTF(this.name);
        out.writeInt(this.age);
        out.writeLong(this.phoneNumber);

        // Company field
        if(this.company == null){
            out.writeBoolean(false);
        }
        else{
            out.writeBoolean(true);
            out.writeUTF(this.company);
        }

        // Email list
        out.writeInt(this.emails.size());
        for(String email : this.emails){
            out.writeUTF(email);
        }

        out.flush();
    }

    public static Contact deserialize(DataInputStream in) throws IOException{
        String name = in.readUTF();
        int age = in.readInt();
        long phoneNumber = in.readLong();

        // Company field
        String company = null;
        boolean hasCompany = in.readBoolean();
        if(hasCompany){
            company = in.readUTF();
        } 

        // Email list
        ArrayList<String> emails = new ArrayList<>();
        int emailsSize = in.readInt();
        for(int i = 0; i < emailsSize; i++){
            emails.add(in.readUTF());
        }

        return new Contact(name, age, phoneNumber, company, emails);
    }

    public String toString () {
        StringBuilder builder = new StringBuilder();
        builder.append(this.name).append(";");
        builder.append(this.age).append(";");
        builder.append(this.phoneNumber).append(";");
        builder.append(this.company).append(";");
        builder.append("{");
        for (String s : this.emails) {
            builder.append(s).append(";");
        }
        builder.append("}");
        return builder.toString();
    }

}
