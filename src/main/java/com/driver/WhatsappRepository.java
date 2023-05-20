package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository {

    HashMap<String,User>userDb=new HashMap<>();
    HashMap<Group, List<User>>groupUserDb=new HashMap<>();
    HashMap<Group,User>adminMap=new HashMap<>();
    HashMap<Group, List<Message>> groupMessageMap=new HashMap<>();
    HashMap<Message, User> senderMap=new HashMap<>();

    int groupCount=0;
    int messageId=0;

    public String createUser (String name,String mobileNo) throws Exception {
        String key = mobileNo;
        if (userDb.containsKey(mobileNo)) {
            throw new Exception("User already exists");
        } else {
            User user = new User(name, mobileNo);
            userDb.put(key, user);

        }
        return "SUCCESS";
    }
    // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
    // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
    // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
    // Note that a personal chat is not considered a group and the count is not updated for personal chats.
    // If group is successfully created, return group.

    //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
    //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.

    public Group createGroup(List<User> users){
        if(users.size() == 2) {
            Group group = new Group(users.get(1).getName(), 2);
            adminMap.put(group,users.get(0));
            groupUserDb.put(group,users);
            groupMessageMap.put(group,new ArrayList<Message>());
            return group;
        }
        this.groupCount += 1;
        Group group =  new Group(new String("Group "+this.groupCount),users.size());
        adminMap.put(group,users.get(0));
        groupUserDb.put(group,users);
        groupMessageMap.put(group,new ArrayList<Message>());
        return group;
    }
    public   int createMessage(String content){

        messageId++;
        Message message=new Message();
        message.setId(messageId);
        message.setContent(content);
        return message.getId();
    }
    public int sendMessage(Message message,User sender,Group group)throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(adminMap.containsKey(group)){
            List<User>userList=groupUserDb.get(group);
            boolean userFound=false;
            for (User user:userList){
                if(user.equals(sender))
                {
                    userFound=true;
                    break;
                }

            }
            if(userFound){
                senderMap.put(message,sender);
                List<Message>messages=groupMessageMap.get(group);
                messages.add(message);
                groupMessageMap.put(group,messages);
                return messages.size();
            }
            throw new Exception("You are not allowed to send message");
        }
        throw new Exception("Group does not exist");
    }
    public String  changeAdmin(User approver, User user, Group group)throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.
        if(adminMap.containsKey(group)){
            if(adminMap.get(group).equals(approver)){
                List<User> participants = groupUserDb.get(group);
                Boolean userFound = false;
                for(User participant: participants){
                    if(participant.equals(user)){
                        userFound = true;
                        break;
                    }
                }
                if(userFound){
                    adminMap.put(group, user);
                    return "SUCCESS";
                }
                throw new Exception("User is not a participant");
            }
            throw new Exception("Approver does not have rights");
        }
        throw new Exception("Group does not exist");
    }
    public int removeUser(User user) throws Exception{
        //This is a bonus problem and does not contain any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group, and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        Boolean userFound=false;
        Group userGroup=null;
        for (Group gp:groupUserDb.keySet()){
            List<User>userList=groupUserDb.get(gp);
            for (User us:userList){
                if (us.equals(user)){
                    if(adminMap.get(gp).equals(user)){
                        throw new Exception("Cannot remove admin");
                    }
                    userGroup=gp;
                    userFound=true;
                    break;

                }
            }
            if (userFound)
                break;
        }
        if (userFound){
            List<User>users=groupUserDb.get(userGroup);
            List<User>updatedUsers=new ArrayList<>();
            for (User participant:users){
                if (participant.equals(user)){

                }else{
                    updatedUsers.add(participant);
                }
            }
            groupUserDb.put(userGroup,updatedUsers);
            HashMap<Message,User>updatedSenderMap=new HashMap<>();
            List<Message>messages=new ArrayList<>();
            List<Message>updatedMessage=new ArrayList<>();
            for (Message message:messages){
                if (senderMap.get(message).equals(user))
                    continue;
                updatedMessage.add(message);
            }
            senderMap=updatedSenderMap;
            return updatedUsers.size()+updatedMessage.size()+updatedSenderMap.size();
        }
        throw new Exception("User not found");
    }
    public String findMessage(Date start,Date end,int key)throws Exception{
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K,
        // throw "K is greater than the number of messages" exception
        List<Message>messages=new ArrayList<>();
        for (Group group:groupMessageMap.keySet()){
            messages.addAll(groupMessageMap.get(group));
        }
        List<Message>filterMessage=new ArrayList<>();
        for (Message message:messages){
            if (message.getTimestamp().after(start)&& message.getTimestamp().before(end)){
                filterMessage.add(message);
            }
        }
        if (filterMessage.size()<key){
            throw new Exception("K is greater than the number of messages");
        }
        Collections.sort(filterMessage, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o2.getTimestamp().compareTo(o1.getTimestamp());
            }
        });
        return filterMessage.get(key-1).getContent();
    }
}
