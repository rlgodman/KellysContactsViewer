<?php
require 'db_connect.php';

if (isset($_GET['id'])){
    $id=trim($_GET['id']);
    
    if($people = $conn->prepare("SELECT * FROM contacts WHERE id =?")){
        $people->bind_param('i',$id);
        $people->execute();
        
        $people->bind_result($id,$unique_ID,$name,$email,$mobile,$home,$address);
        
        $contact = array();
        while($people->fetch()){
            $contact["id"]=$id;
            $contact["Name"]=$name;
	    $contact["Email_Address"]=$email;	
            $contact["Mobile_Number"]=$mobile;
            $contact["Home_Number"]=$home;
            $contact["Address"]=$address;
        }
        //$response ["success"]=1;
        $response ["contact"]=array();
        $response =$contact;
        
        //echo json response
        echo json_encode($response);
    } else {
        //failed 
        $response["success"]=0;
        $response["message"]="No contact found ";
        
        //echo json repsonse
        echo json_encode($response);
    }
} else {
    //require field missing
    $response["success"]=0;
    $response["message"]="Required field missing";
    
    //echo json response
    echo json_encode($response);   
}
?>