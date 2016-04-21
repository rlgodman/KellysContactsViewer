<?php
//array for json response
$response = array();

//check for required fields
if(isset($_GET['id'], $_GET['name'],$_GET['email'], $_GET['mobile'], $_GET['home'],$_GET['address'])){
    $id= trim($_GET['id']);
    $name= trim($_GET['name']);
    $email= trim($_GET['email']);
    $mobile= trim($_GET['mobile']);
    $home= trim($_GET['home']);
    $address= trim($_GET['address']);
    
    //db connect class
    require 'db_connect.php';
    
    if($update = $conn->query("UPDATE contacts SET Name='$name', Email_Address ='$email', Mobile_Number='$mobile', Home_Number='$home',Address='$address'WHERE id='$id'")){
        //successfully updated 
        $response["success"]=1;
        $response["message"]="contact updated";
        //echo JSON
        echo json_encode($response);
    } else {
        //failed to update row
        $response["success"]=0;
        $response["message"]="An error occured";
        //echo JSON 
        echo json_encode($response);
    }
} else {
    //required field is missing
    $response["success"]=0;
    $response["message"]="required field missing";
    
    //echo JSON 
    echo json_encode($response);
}
?>