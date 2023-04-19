package com.example.iot_proj2;

public class StaticStrings {
    public static String[] FacultyString = {"Faculty of Accounting and Informatics",
            "Faculty of Applied Sciences" ,
            "Faculty of Arts and Design" ,
            "Faculty of Engineering and the Built Environment" ,
            "Faculty of Health Sciences" ,
            "Faculty of Management Sciences"};

    public static String[][] DepartmentString = {
            {"Information Technology" , "Information Systems"},
            {"Statistics"},
            {"Drama and Production Studies"},
            {"Architecture"},
            {"Dental Sciences"},
            {"Applied Law"}
    };

    public static String[][][] ModuleString = {

            {
                // For IT Department
                    {"APDA101","APDP101",
                            "APDA201","APDP201",
                            "APDA301","APDP301",
                            "OSYS101",
                            "MCPA201"},

                    // For IS Department
                    {"ISYA101", "ISYA201",
                            "ISYA301",
                            "INMA201"}

            },
            {
                // For Statistics Department
                    {"APMC401","APPS101"}
            },
            {
                // For drama and production studies
                    {"ACTA101","ACTA201"}
            },
            {
                // For architecture
                    {"ADNA101", "PHBA101"}
            },
            {
                 // For dental sciences
                    {"DMST111","DMST211"}
            },
            {
                // For Law
                    {"BSNL101","LBRL201"}
            }

    };

    public static String[] TimeString = {"09:00", "09:30",
            "10:00", "10:30",
            "11:00",  "11:30",
            "12:00",  "12:30",
           "13:00",  "13:30",
            "14:00",  "14:30",
            "15:00"};

    public static String[] TimeStringForView = {"09:00 AM", "09:30 AM",
            "10:00 AM", "10:30 AM",
            "11:00 AM",  "11:30 AM",
            "12:00 PM",  "12:30 PM",
            "01:00 PM",  "01:30 PM",
            "02:00 PM",  "02:30 PM",
            "03:00 PM"};


    public static String[] AppointmentReasons = {
            "Discuss available vacancy",
            "Clarification of course material",
            "Discuss tutoring strategies",
            "Discuss student progress",
            "Seek feedback",
            "Personal matters"
    };

    public static String hashPassword(String password)
    {
        try {
            String key = "IOTGroup";
            StringBuilder ciphertext = new StringBuilder();
            for (int i = 0; i < password.length(); i++) {
                char p = password.charAt(i);
                char k = key.charAt(i % key.length());
                char c = (char) (p ^ k);
                ciphertext.append(c);
            }
            return ciphertext.toString();
        } catch(Exception e)
        {
            return e.toString();
        }
    }
}
