<?php

//array for JSON response
$response = array();

//check for required fields
if (isset($_GET['name'], $_GET['email'], $_GET['mobile'], $_GET['home'], $_GET['address'])) {


    $name = trim($_GET['name']);
    $email = trim($_GET['email']);
    $mobile = trim($_GET['mobile']);
    $home = trim($_GET['home']);
    $address = trim($_GET['address']);

    require 'db_connect.php';

    if ($insert = $conn->query("INSERT INTO contacts (Name,Email_Address,Mobile_Number,Home_Number,Address) 
        VALUES ('{$name}','{$email}','{$mobile}','{$home}','{$address}')")) {
        //successfully inserted
        $response["success"] = 1;
        $response["message"] = "Contact successfully created.";

        //echo json response
        echo json_encode($response);
    } else {
        //failed to insert row
        $response["success"] = 0;
        $response["message"] = "An error occurred";

        //encode json response
        echo json_encode($response);
    }
} else {
    //required field is missing
    $response["success"] = 0;
    $response["message"] = "required field is missing";
    //echo json response
    echo json_encode($response);
}

?>