Mohammad Mahameed - 314933870
Abed Jbareen      - 322627563

Git repository: https://github.com/Mohammad-Mahameed/Distributive_Systems.git
Branch: release

How to build and run:
    Place your credentials in /$HOME/.aws/credentials - where credentials is NOT a txt file.
    Creation guide (script):
        cd $HOME
        mkdir .aws
        cd .aws
        touch credentials
    
    Edit credentials file with any convenient text editor (Gedit is recommended for VM/Linux desktop users, and Notepad for WSL users) and fill it in with yours.
    

    -Build:
        Option A: Execute: mvn clean install.
        Option B: run the "build.nsh" script (execute: ./build.nsh).
    -Run:
        Option A: Execute: mvn exec:java -Dexec.mainClass="com.example.LocalApp" -Dexec.cleanupDaemonThreads=false -Dexec.args="input1.txt input2.txt input3.txt input4.txt input5.txt output1.html output2.html output3.html output4.html output5.html n terminate"
                  Along with specifying your input and output files' names (5 is a MAGIC number), also specifying N as predefined in the assignment.
        Option B: run the "run_LocalApp.nsh" script. Please care to modify the script accordingly (e.g updating the -Dexec.args="" flag content).


Used AMI: ami-00e95a9222311e8ed
Execution time for the 5 given input files: ~35 minutes
Used N: 1

-Did you think for more than 2 minutes about security? Do not send your credentials in plaintext!
    We for sure haven't sent the credentials as a plaintext, they were located in /$HOME/.aws/credentials and encrypted through the communication channel with AWS servers.

-Did you think about scalability? Will your program work properly when 1 million clients connected at the same time? How about 2 million? 1 billion? Scalability is very important aspect of the system; be sure it is scalable!
    It can handle as many as Local applications are out there :).

-What about persistence? What if a node dies? What if a node stalls for a while? Have you taken care of all possible outcomes in the system? Think of more possible issues that might arise from failures. What did you do to solve it? What about broken communications? Be sure to handle all fail-cases!
    If a node dies, the system would simply create a new one.

-Threads in your application, when is it a good idea? When is it bad? Invest time to thinkabout threads in your application!
    We've parallelized the Manager's work, where he has to deal two ends: Workers and Local applications.

-Did you run more than one client at the same time? Be sure they work properly, and finish properly, and your results are correct.
    We did. Everything was fine.

-Do you understand how the system works? Do a full run using pen and paper, draw the different parts and the communication that happens between them.
    SURE THING!

-Did you manage the termination process? Be sure all is closed once requested!
    When a termination message is received, every AWS resource (e.g Buckets and SQS queues) is deleted, along with EC2 instaces (e.g workers/manager) terminations.

-Are all your workers working hard? Or some are slacking? Why?
    Each worker works as hard as it can, and it's potential is used to it's fullest.

-Is your manager doing more work than he's supposed to? Have you made sure each part of your system has properly defined tasks? Did you mix their tasks? Don't!
    He is not! Tasks are spread along the workers.

-Lastly, are you sure you understand what distributed means? Is there anything in your system awaiting another?
    We for sure understand what's a DISTRIBUTED SYSTEM is. Nothing in our project is awaiting others, except for Local applications.
