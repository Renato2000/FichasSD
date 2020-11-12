import java.util.*;
import java.util.concurrent.locks.*;

class Bank {

    private static class Account {
        private int balance;
        public ReentrantLock lock = new ReentrantLock();
        Account(int balance) { this.balance = balance; }
        int balance() { return balance; }
        boolean deposit(int value) {
            balance += value;
            return true;
        }
        boolean withdraw(int value) {
            if (value > balance)
                return false;
            balance -= value;
            return true;
        }
    }

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    private ReentrantLock lockBanco = new ReentrantLock();

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        int id;

        lockBanco.lock();
        try{
            id = nextId;
            nextId += 1;
            map.put(id, c);
        } finally{
            lockBanco.unlock();
        }

        return id;
    }

    /*
    Temos de dar lock à conta, uma vez que poderia haver uma operação em processo (como uma transferencia)
    */
    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        lockBanco.lock();
        try{
            c = map.remove(id);
            if (c == null)
                return 0;

            c.lock.lock();
        } finally{
            lockBanco.unlock();
        }

        try{
            return c.balance();
        } finally{
            c.lock.unlock();
        }
    }

    /*
    Como não sabemos se o get é thread safe, temos de utilizar locks.
    Poderia ser removida ao fazer c.balance()

    lockBanco.lock();
    1. verificar se a conta existe
    lockBanco.unlock();
    lockConta.lock();
    2. se existe, retorna saldo
    lockConta.unlock();
    */
    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        lockBanco.lock();
        try{
            c = map.get(id);
            if (c == null)
                return 0;
            c.lock.lock();
        } finally{
            lockBanco.unlock();
        }

        try{
            return c.balance();
        } finally{
            c.lock.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        lockBanco.lock();

        try{
            c = map.get(id);
            if (c == null)
                return false;

            c.lock.lock();
        } finally{
            lockBanco.unlock();
        }

        try{
            return c.deposit(value);
        } finally{
            c.lock.unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        lockBanco.lock();

        try{
            c = map.get(id);
            if (c == null)
                return false;

            c.lock.lock();
        } finally{
            lockBanco.unlock();
        }

        try{
            return c.withdraw(value);
        } finally{
            c.lock.unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        lockBanco.lock();

        try{    
            cfrom = map.get(from);
            cto = map.get(to);
            if (cfrom == null || cto ==  null)
                return false;
            
            cfrom.lock.lock();
            cto.lock.lock();
        } finally{
            lockBanco.unlock();
        }

        try{
            return cfrom.withdraw(value) && cto.deposit(value);
        } finally{
            cfrom.lock.unlock();
            cto.lock.unlock();
        }

    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        int total = 0;
        Account[] acs = new Account[ids.length]; //vai ser necessário guardar as contas, porque o map não é thread safe.

        lockBanco.lock();
        try{
            for (int i : ids) {
            	if(!map.containsKey(i)){
            		lockBanco.lock();
            		return 0;
            	}
            	Account c = map.get(i);
                acs[i] = c;
            	//c.lock.lock();                
            }

            for(Account c : acs){
                c.lock.lock();
            }
        } finally{
            lockBanco.unlock();
        }

        for(Account c : acs){
            total += c.balance();
            c.lock.unlock();
        }
        
        return total;
    }
}