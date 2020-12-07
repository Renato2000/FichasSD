import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

class ContactListValueObject {
    private List<Contact> contactList;

    public ContactListValueObject(ArrayList<Contact> contacts) {
        this.contactList = contacts;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(contactList.size());
        for(Contact c : contactList){
            c.serialize(out);
        }
        out.flush();
    }

    public static ContactListValueObject deserialize(DataInputStream in) throws IOException {
    	int nrContacts = in.readInt();
    	ArrayList<Contact> contacts = new ArrayList<>();
        for(int i = 0; i < nrContacts; i++){
        	Contact c = Contact.deserialize(in); 
            contacts.add(c);
        }
        return new ContactListValueObject(contacts);
    }
    
}