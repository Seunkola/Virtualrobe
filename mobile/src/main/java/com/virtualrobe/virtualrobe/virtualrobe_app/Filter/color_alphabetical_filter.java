package com.virtualrobe.virtualrobe.virtualrobe_app.Filter;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class color_alphabetical_filter {

    private String Collection;
    private String Character;
    private FirebaseFirestore firebaseFirestore;

    public color_alphabetical_filter(String Collection,String Character, FirebaseFirestore firebaseFirestore){
        this.Collection = Collection;
        this.Character = Character;
        this.firebaseFirestore = firebaseFirestore;
    }

    public Query filter_by_alphabets(){
    Query query = firebaseFirestore.collection(Collection);
        if (Character.equalsIgnoreCase("A")){
            query = firebaseFirestore.collection(Collection)
                    .whereLessThan("name","B");
        }
        if (Character.equalsIgnoreCase("B")){
            query = firebaseFirestore.collection(Collection)
                    .whereLessThan("name","C")
                    .whereGreaterThan("name","A");
        }

        return query;
    }

}
