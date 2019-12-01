package com.example.mrservice.models;

public class ContactUsModel {
    String input_name, input_email,input_subject,input_message;


    public ContactUsModel(String Name, String Email, String Subject, String Message) {
        this.input_name = Name;
        this.input_email = Email;
        this.input_subject = Subject;
        this.input_message = Message;
    }

    public String getInput_name () {
        return input_name;
    }

    public String getInput_email () {
        return input_email;
    }

    public String getInput_subject  () {
        return input_subject;
    }

    public String getInput_message () {
        return input_message;
    }


}

