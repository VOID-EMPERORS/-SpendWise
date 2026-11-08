package com.spendwise;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Database.initialize();
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Cannot connect to MySQL. Check that MySQL is running and credentials in environment variables are correct.\\n\\n"+e.getMessage(),
                    "SpendWise - Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    static final class Theme {
        static final Color BG=new Color(12,19,32), SIDEBAR=new Color(17,29,47),
            PANEL=new Color(24,38,59), PANEL2=new Color(31,48,73),
            TEXT=new Color(239,244,252), MUTED=new Color(154,171,195),
            BLUE=new Color(74,139,245), GREEN=new Color(42,190,135),
            RED=new Color(238,91,107), AMBER=new Color(238,177,76);
        static JLabel label(String s,int size,Color color,boolean bold){
            JLabel l=new JLabel(s);l.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size));l.setForeground(color);return l;
        }
        static JButton button(String s,Color color){
            JButton b=new JButton(s);b.setFont(new Font("Segoe UI",Font.BOLD,13));b.setForeground(Color.WHITE);
            b.setBackground(color);b.setFocusPainted(false);b.setBorder(new EmptyBorder(11,16,11,16));return b;
        }
        static JPanel card(){JPanel p=new JPanel();p.setBackground(PANEL);p.setBorder(new EmptyBorder(19,20,19,20));p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));return p;}
        static void styleTable(JTable t){
            t.setRowHeight(34);t.setFont(new Font("Segoe UI",Font.PLAIN,13));t.setForeground(TEXT);
            t.setBackground(PANEL);t.setGridColor(PANEL2);t.setSelectionBackground(new Color(48,76,112));
            t.setSelectionForeground(Color.WHITE);t.getTableHeader().setBackground(PANEL2);
            t.getTableHeader().setForeground(TEXT);t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        }
    }

    static class LoginFrame extends JFrame {
        private final FinanceRepository repo=new FinanceRepository();
        private final JTextField name=new JTextField(),email=new JTextField();
        private final JPasswordField password=new JPasswordField();
        private boolean registering=false;
        private final JLabel title=Theme.label("Welcome to SpendWise",27,Theme.TEXT,true);
        private final JPanel nameRow=field("Full name",name);

        LoginFrame(){
            setTitle("SpendWise | Personal Expense Tracker");setSize(920,590);setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE);setLayout(new GridLayout(1,2));
            JPanel hero=new JPanel();hero.setBackground(new Color(18,48,80));hero.setBorder(new EmptyBorder(65,48,50,35));
            hero.setLayout(new BoxLayout(hero,BoxLayout.Y_AXIS));
            hero.add(Box.createVerticalGlue());hero.add(Theme.label("SpendWise",38,Color.WHITE,true));
            hero.add(Box.createVerticalStrut(12));hero.add(Theme.label("Make every rupee count.",20,new Color(190,218,247),false));
            hero.add(Box.createVerticalStrut(28));hero.add(Theme.label("<html>Track income.<br>Control expenses.<br>Build smarter money habits.</html>",17,Color.WHITE,false));hero.add(Box.createVerticalGlue());
            add(hero);
            JPanel right=new JPanel(new BorderLayout(0,24));right.setBackground(Theme.BG);right.setBorder(new EmptyBorder(65,60,55,60));
            JPanel head=new JPanel();head.setOpaque(false);head.setLayout(new BoxLayout(head,BoxLayout.Y_AXIS));
            head.add(title);head.add(Box.createVerticalStrut(8));head.add(Theme.label("Sign in or create an account to continue",13,Theme.MUTED,false));right.add(head,BorderLayout.NORTH);
            JPanel fields=new JPanel();fields.setOpaque(false);fields.setLayout(new BoxLayout(fields,BoxLayout.Y_AXIS));
            nameRow.setVisible(false);fields.add(nameRow);fields.add(field("Email address",email));fields.add(field("Password (minimum 6 characters)",password));
            JButton submit=Theme.button("Sign in",Theme.BLUE);submit.addActionListener(e->submit());
            JButton toggle=new JButton("New to SpendWise? Create an account");toggle.setForeground(Theme.BLUE);toggle.setContentAreaFilled(false);toggle.setBorderPainted(false);
            toggle.addActionListener(e->{registering=!registering;nameRow.setVisible(registering);title.setText(registering?"Create your account":"Welcome to SpendWise");submit.setText(registering?"Create account":"Sign in");toggle.setText(registering?"Already registered? Sign in":"New to SpendWise? Create an account");});
            JPanel actions=new JPanel(new GridLayout(2,1,0,9));actions.setOpaque(false);actions.add(submit);actions.add(toggle);
            JPanel center=new JPanel(new BorderLayout(0,20));center.setOpaque(false);center.add(fields,BorderLayout.CENTER);center.add(actions,BorderLayout.SOUTH);
            right.add(center,BorderLayout.CENTER);add(right);
        }
        JPanel field(String caption,JComponent input){
            JPanel p=new JPanel(new BorderLayout(0,7));p.setOpaque(false);p.setBorder(new EmptyBorder(0,0,15,0));
            JLabel l=Theme.label(caption,12,Theme.MUTED,true);input.setFont(new Font("Segoe UI",Font.PLAIN,14));input.setPreferredSize(new Dimension(250,40));
            p.add(l,BorderLayout.NORTH);p.add(input,BorderLayout.CENTER);return p;
        }
        void submit(){
            String em=email.getText().trim(),pw=new String(password.getPassword());
            if(!em.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")||pw.length()<6){error("Enter a valid email and a password of at least 6 characters.");return;}
            try{
                FinanceRepository.User user;
                if(registering){
                    String nm=name.getText().trim();if(nm.isBlank()){error("Please enter your full name.");return;}
                    int id=repo.register(nm,em,pw);user=new FinanceRepository.User(id,nm,em);
                }else user=repo.login(em,pw);
                if(user==null){error("Email or password is incorrect.");return;}
                new DashboardFrame(user).setVisible(true);dispose();
            }catch(java.sql.SQLIntegrityConstraintViolationException ex){error("This email is already registered.");}
            catch(Exception ex){error(ex.getMessage());}
        }
        void error(String s){JOptionPane.showMessageDialog(this,s,"SpendWise",JOptionPane.ERROR_MESSAGE);}
    }

    static class DashboardFrame extends JFrame {
        final FinanceRepository repo=new FinanceRepository();
        final FinanceRepository.User user;
        final JPanel content=new JPanel(new BorderLayout());
        String currentPage="Dashboard";
        DashboardFrame(FinanceRepository.User user){
            this.user=user;setTitle("SpendWise | "+user.name());setSize(1250,780);setMinimumSize(new Dimension(1020,650));
            setLocationRelativeTo(null);setDefaultCloseOperation(EXIT_ON_CLOSE);setLayout(new BorderLayout());
            JPanel side=new JPanel();side.setPreferredSize(new Dimension(225,0));side.setBackground(Theme.SIDEBAR);
            side.setBorder(new EmptyBorder(27,17,22,17));side.setLayout(new BoxLayout(side,BoxLayout.Y_AXIS));
            side.add(Theme.label("SpendWise",25,Color.WHITE,true));side.add(Box.createVerticalStrut(5));
            side.add(Theme.label("PERSONAL FINANCE",10,Theme.MUTED,true));side.add(Box.createVerticalStrut(32));
            for(String page:List.of("Dashboard","Transactions","Budgets","Reports")){
                JButton b=Theme.button("   "+page,Theme.SIDEBAR);b.setHorizontalAlignment(SwingConstants.LEFT);
                b.setMaximumSize(new Dimension(190,43));b.addActionListener(e->showPage(page));side.add(b);side.add(Box.createVerticalStrut(8));
            }
            side.add(Box.createVerticalGlue());side.add(Theme.label("SIGNED IN AS",10,Theme.MUTED,true));
            side.add(Box.createVerticalStrut(5));side.add(Theme.label(user.name(),14,Theme.TEXT,true));
            JButton logout=Theme.button("Sign out",new Color(60,73,92));logout.addActionListener(e->{new LoginFrame().setVisible(true);dispose();});
            side.add(Box.createVerticalStrut(14));side.add(logout);
            content.setBackground(Theme.BG);add(side,BorderLayout.WEST);add(content,BorderLayout.CENTER);showPage("Dashboard");
        }
        JPanel page(String title,String subtitle){
            JPanel p=new JPanel(new BorderLayout(0,22));p.setBackground(Theme.BG);p.setBorder(new EmptyBorder(30,34,30,34));
            JPanel h=new JPanel();h.setOpaque(false);h.setLayout(new BoxLayout(h,BoxLayout.Y_AXIS));
            h.add(Theme.label(title,27,Theme.TEXT,true));h.add(Box.createVerticalStrut(6));h.add(Theme.label(subtitle,13,Theme.MUTED,false));
            p.add(h,BorderLayout.NORTH);return p;
        }
        void showPage(String page){currentPage=page;content.removeAll();
            switch(page){case "Dashboard"->dashboard();case "Transactions"->transactions();case "Budgets"->budgets();case "Reports"->reports();}
            content.revalidate();content.repaint();
        }
        void dashboard(){
            JPanel p=page("Dashboard","Your personal finance overview at a glance.");
            try{
                FinanceRepository.Summary s=repo.summary(user.id());
                JPanel cards=new JPanel(new GridLayout(1,3,14,0));cards.setOpaque(false);
                cards.add(stat("TOTAL BALANCE",s.balance(),Theme.BLUE));cards.add(stat("TOTAL INCOME",s.income(),Theme.GREEN));cards.add(stat("TOTAL EXPENSES",s.expenses(),Theme.RED));
                JPanel body=new JPanel(new BorderLayout(0,18));body.setOpaque(false);body.add(cards,BorderLayout.NORTH);
                JPanel recent=new JPanel(new BorderLayout(0,10));recent.setOpaque(false);recent.add(Theme.label("Recent transactions",18,Theme.TEXT,true),BorderLayout.NORTH);
                recent.add(new JScrollPane(transactionTable("",8)),BorderLayout.CENTER);body.add(recent,BorderLayout.CENTER);
                p.add(body,BorderLayout.CENTER);
            }catch(Exception ex){error(ex.getMessage());}
            content.add(p);
        }
        JPanel stat(String title,BigDecimal value,Color accent){
            JPanel c=Theme.card();c.add(Theme.label(title,11,Theme.MUTED,true));c.add(Box.createVerticalStrut(13));
            c.add(Theme.label("₹ "+value.toPlainString(),25,accent,true));return c;
        }
        JTable transactionTable(String search,int limit) throws Exception{
            List<FinanceRepository.Transaction> rows=repo.transactions(user.id(),search);
            DefaultTableModel m=new DefaultTableModel(new String[]{"Date","Type","Category","Description","Amount (₹)","ID"},0){
                public boolean isCellEditable(int r,int c){return false;}
            };
            for(int i=0;i<Math.min(rows.size(),limit);i++){var t=rows.get(i);m.addRow(new Object[]{t.date(),t.type(),t.category(),t.description(),t.amount(),t.id()});}
            JTable table=new JTable(m);Theme.styleTable(table);table.getColumnModel().getColumn(5).setMinWidth(0);table.getColumnModel().getColumn(5).setMaxWidth(0);return table;
        }
        void transactions(){
            JPanel p=page("Transactions","Add, search and review your financial records.");
            JTextField search=new JTextField(19);search.setFont(new Font("Segoe UI",Font.PLAIN,13));
            JButton find=Theme.button("Search",Theme.PANEL2),add=Theme.button("+ Add transaction",Theme.BLUE),del=Theme.button("Delete selected",Theme.RED);
            JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));actions.setOpaque(false);
            actions.add(search);actions.add(find);actions.add(add);actions.add(del);
            JTable[] table={null};JPanel tableWrap=new JPanel(new BorderLayout());tableWrap.setOpaque(false);
            Runnable refresh=()->{try{tableWrap.removeAll();table[0]=transactionTable(search.getText(),1000);tableWrap.add(new JScrollPane(table[0]));tableWrap.revalidate();tableWrap.repaint();}catch(Exception ex){error(ex.getMessage());}};
            find.addActionListener(e->refresh.run());search.addActionListener(e->refresh.run());
            add.addActionListener(e->addTransaction(refresh));del.addActionListener(e->{if(table[0]==null||table[0].getSelectedRow()<0){error("Select a transaction first.");return;}
                long id=((Number)table[0].getValueAt(table[0].getSelectedRow(),5)).longValue();
                if(JOptionPane.showConfirmDialog(this,"Delete selected transaction?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
                    try{repo.deleteTransaction(user.id(),id);refresh.run();}catch(Exception ex){error(ex.getMessage());}});
            JPanel body=new JPanel(new BorderLayout(0,13));body.setOpaque(false);body.add(actions,BorderLayout.NORTH);body.add(tableWrap,BorderLayout.CENTER);p.add(body,BorderLayout.CENTER);
            refresh.run();content.add(p);
        }
        void addTransaction(Runnable refresh){
            JComboBox<String> type=new JComboBox<>(new String[]{"EXPENSE","INCOME"});
            JComboBox<String> category=new JComboBox<>(new String[]{"Food","Travel","Shopping","Education","Bills","Health","Entertainment","Salary","Freelance","Other"});
            JTextField amount=new JTextField(),date=new JTextField(LocalDate.now().toString()),description=new JTextField();
            JPanel form=new JPanel(new GridLayout(0,2,10,10));form.add(new JLabel("Transaction type"));form.add(type);
            form.add(new JLabel("Category"));form.add(category);form.add(new JLabel("Amount (₹)"));form.add(amount);
            form.add(new JLabel("Date (YYYY-MM-DD)"));form.add(date);form.add(new JLabel("Description"));form.add(description);
            if(JOptionPane.showConfirmDialog(this,form,"Add transaction",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
            try{
                BigDecimal a=new BigDecimal(amount.getText().trim());if(a.signum()<=0)throw new IllegalArgumentException("Amount must be greater than zero.");
                repo.addTransaction(user.id(),(String)type.getSelectedItem(),(String)category.getSelectedItem(),a,LocalDate.parse(date.getText().trim()),description.getText().trim());
                JOptionPane.showMessageDialog(this,"Transaction saved successfully.");refresh.run();
            }catch(Exception ex){error("Please check the entered values. "+ex.getMessage());}
        }
        void budgets(){
            JPanel p=page("Monthly budgets","Set category-wise spending limits for "+YearMonth.now()+".");
            JButton set=Theme.button("+ Set / update budget",Theme.BLUE);set.addActionListener(e->setBudget());
            JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));top.setOpaque(false);top.add(set);
            JPanel body=new JPanel(new BorderLayout(0,13));body.setOpaque(false);body.add(top,BorderLayout.NORTH);
            try{
                String[] cols={"Category","Monthly limit (₹)","Spent (₹)","Remaining (₹)","Status"};
                DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
                for(var b:repo.budgets(user.id(),YearMonth.now()))m.addRow(new Object[]{b.category(),b.limit(),b.spent(),b.remaining(),b.spent().compareTo(b.limit())>0?"Over budget":"Within budget"});
                JTable t=new JTable(m);Theme.styleTable(t);body.add(new JScrollPane(t),BorderLayout.CENTER);
            }catch(Exception ex){error(ex.getMessage());}
            p.add(body,BorderLayout.CENTER);content.add(p);
        }
        void setBudget(){
            JComboBox<String> cat=new JComboBox<>(new String[]{"Food","Travel","Shopping","Education","Bills","Health","Entertainment","Other"});
            JTextField amount=new JTextField();JPanel f=new JPanel(new GridLayout(0,2,10,10));
            f.add(new JLabel("Category"));f.add(cat);f.add(new JLabel("Monthly limit (₹)"));f.add(amount);
            if(JOptionPane.showConfirmDialog(this,f,"Set monthly budget",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
            try{BigDecimal n=new BigDecimal(amount.getText().trim());if(n.signum()<=0)throw new IllegalArgumentException("Budget must be greater than zero.");
                repo.saveBudget(user.id(),(String)cat.getSelectedItem(),YearMonth.now(),n);JOptionPane.showMessageDialog(this,"Budget saved.");showPage("Budgets");
            }catch(Exception ex){error(ex.getMessage());}
        }
        void reports(){
            JPanel p=page("Reports & analytics","Expense breakdown for the last 30 days.");
            try{
                Map<String,BigDecimal> data=repo.expenseByCategory(user.id());
                JPanel panel=Theme.card();panel.add(Theme.label("Category-wise expenses",18,Theme.TEXT,true));panel.add(Box.createVerticalStrut(15));
                if(data.isEmpty())panel.add(Theme.label("No expense records found in the last 30 days.",14,Theme.MUTED,false));
                else panel.add(new BarChart(data));
                p.add(panel,BorderLayout.CENTER);
            }catch(Exception ex){error(ex.getMessage());}
            content.add(p);
        }
        void error(String message){JOptionPane.showMessageDialog(this,message,"SpendWise",JOptionPane.ERROR_MESSAGE);}
    }

    static class BarChart extends JPanel {
        final Map<String,BigDecimal> data;
        BarChart(Map<String,BigDecimal> data){this.data=data;setOpaque(false);setPreferredSize(new Dimension(650,390));}
        @Override protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int left=150,top=20,barH=27,gap=17,maxW=Math.max(100,getWidth()-220);
            double max=data.values().stream().mapToDouble(BigDecimal::doubleValue).max().orElse(1);
            int i=0;for(var e:data.entrySet()){
                int y=top+i*(barH+gap);g.setColor(Theme.MUTED);g.setFont(new Font("Segoe UI",Font.PLAIN,13));
                g.drawString(e.getKey(),10,y+19);
                int w=(int)(e.getValue().doubleValue()/max*maxW);
                g.setColor(Theme.BLUE);g.fillRoundRect(left,y,Math.max(4,w),barH,9,9);
                g.setColor(Theme.TEXT);g.drawString("₹ "+e.getValue().toPlainString(),left+w+9,y+19);i++;
            }g.dispose();
        }
    }
}
