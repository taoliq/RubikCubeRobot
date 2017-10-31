#include <AccelStepper.h>
#include <MultiStepper.h>

#define SPEED 1200
#define ACCELERATION 900

//control mode define
#define SOLVE 0
#define INIT 1
#define LOAD 2
#define MANUAL 3

AccelStepper stepper0(AccelStepper::DRIVER, 2, 3);
AccelStepper stepper1(AccelStepper::DRIVER, 4, 5);
AccelStepper stepper2(AccelStepper::DRIVER, 6, 7);
AccelStepper stepper3(AccelStepper::DRIVER, 8, 9);
AccelStepper stepper4(AccelStepper::DRIVER, 10, 11);
AccelStepper stepper5(AccelStepper::DRIVER, 12, 13);

AccelStepper* stepper[6] = {
  &stepper0, &stepper1, &stepper2, &stepper3,
  &stepper4, &stepper5,
};

MultiStepper multiStepper[2];

char cubeStatus = 'O';  //means cube's Status in getting colors
int isxRotate = 0; //Did chube have x rotate?
int ctrlMode = SOLVE;
String comdata = "";
int step[2][3] = {400, -400, -800,
  -460, 450, 0};
int offset[4] = {10, -10, -10, 0}; //operation FRBL offsets
int totalError[4] = {0, 0, 0, 0};
long position[2];

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  for (int i = 0; i < 6; i++) {
    stepper[i]->setMaxSpeed(SPEED * 2);
    stepper[i]->setSpeed(SPEED);
    stepper[i]->setAcceleration(ACCELERATION);
  }

  multiStepper[0].addStepper(*stepper[0]);
  multiStepper[0].addStepper(*stepper[2]);
  multiStepper[1].addStepper(*stepper[1]);
  multiStepper[1].addStepper(*stepper[3]);

  while (!Serial);
}

void loop() {
  // put your main code here, to run repeatedly
  getCom();
  exeCom();
  // Serial.println(ctrlMode);
}

void getCom() {
  while (Serial.available()) {
    char c = Serial.read();
//    Serial.println(c);
  comdata += c;
  delay(2);
  }
}

void exeCom() {
  if (comdata == "" && ctrlMode != MANUAL) return;
  if (comdata != "")
    Serial.println(comdata);  
  if (comdata.length() && comdata[0] == ':') {
    if (comdata.substring(1) == "Solve")  ctrlMode = SOLVE;
    if (comdata.substring(1) == "Init")   ctrlMode = INIT;
    if (comdata.substring(1) == "Load")   ctrlMode = LOAD;
    if (comdata.substring(1) == "Manual") ctrlMode = MANUAL;    
    // if (ctrlMode == "Load") {
    //   init_cube();
    //   rotate_cube();
    // }
    // return;Manual
    comdata = "";    
  }
  // if (comdata == "Init") {
  //   init_cube();
  //   rotate_cube();
  // } else if (comdata == "Rot")
  //   rotate_cube();
  // else
  //   solve();

  switch (ctrlMode) {
    case SOLVE: solve(); break;
    case INIT: init_cube(); ctrlMode = LOAD; comdata = "Rot";
    case LOAD: rotate_cube(); break;
    case MANUAL: man_ctrl(); break;
    default: break;
  }
  
  // if (ctrlMode == "Load") {
  //   rotate_cube();
  // } 
  // if (ctrlMode == "Manual") {
  //   man_ctrl();
  // } 
  // if (ctrlMode == "Solve") {
  //   solve();
  // }
  comdata = "";
}

void init_cube() {
    cubeStatus = 'O'; 
    isxRotate = 0;
    for (int i = 0; i < 4; i++)
      totalError[i] = 0;
}

void rotate_cube() {
  if (comdata == "") return;
  switch (cubeStatus) {
    case 'O': 
      baseMove("51715041"); 
      cubeStatus = 'U'; 
      break;
    case 'U': 
      baseMove("72"); 
      cubeStatus = 'D'; 
      break;
    case 'D': 
      baseMove("70614051");
      cubeStatus = 'F'; 
      break;
    case 'F': 
      baseMove("62"); 
      cubeStatus = 'B'; 
      break;
    case 'B': 
      baseMove("605041614051"); 
      cubeStatus = 'L'; 
      break;
    case 'L': 
      baseMove("62"); 
      cubeStatus = 'R'; 
      break;
    case 'R': 
      baseMove("617150417040"); 
      cubeStatus = 'Z'; 
      break;
    default: break;
  }
}

//control machine directly by android
void man_ctrl() {
  static int id = 0;
  static int ori = 0;
  static int dis = 1;
  if (comdata.length()) {
    id = comdata[0] - '0';
    ori = comdata[1] - '0' - 1;
  }
  if (ori) {
    stepper[id]->move(dis * ori);
      // while (stepper[id]->run());
      // Serial.println(e);
      // Serial.println(step[id / 4][ori] + e);
  //    stepper[id]->runToPosition();
  //    Serial.println(stepper[id]->speed());
  //    stepper[id]->stop();
  
    while (stepper[id]->distanceToGo() != 0) {
      stepper[id]->setSpeed(500);
      stepper[id]->runSpeedToPosition();
    }
  } else {
    // id = ori = 0;
    stepper[id]->stop();
  }
  // Serial.println("Quit man_ctrl()");
}

void solve() {
  // Serial.println(comdata);
  for (int i = 0; i < comdata.length(); i++) {
    if (comdata[i] == '\'' || comdata[i] == '2')
      continue;
    char ori = ' ';
    if (i != comdata.length() - 1
        && (comdata[i+1] == '\'' || comdata[i+1] == '2'))
      ori = comdata[i+1];
    //Serial.println(comdata[i]);
    //Serial.println(ori);
    move(comdata[i], ori);
  }
}

void move(char face, char ori) {
  switch (face) {
    // case 'U': moveU(ori); break;
    // case 'F': moveF(ori); break;
    case 'U': moveU(ori); break;
    case 'F': moveF(ori); break;
    case 'R': moveR(ori); break;
    case 'L': moveL(ori); break;
    // case 'B': moveB(ori); break;
    // case 'D': moveD(ori); break;
    case 'B': moveB(ori); break;
    case 'D': moveD(ori); break;
    case 'x': rotateX(ori); break;
    case 'z': rotateZ(ori); break;
    default: break;
  }
}

void moveF(char ori) {
  String com = "";
  switch (ori) {
    case ' ':   com = "00410140"; break;
    case '\'':  com = "01410040"; break;
    case '2':   com = "02"; break;
  }
  baseMove(com);
}

void moveR(char ori) {
  String com = "";
  switch (ori) {
    case ' ':   com = "10511150"; break;
    case '\'':  com = "11511050"; break;
    case '2':   com = "12"; break;
  }
  baseMove(com);
}

void moveB(char ori) {
  String com = "";
  switch (ori) {
    case ' ':   com = "20412140"; break;
    case '\'':  com = "21412040"; break;
    case '2':   com = "22"; break;
  }
  baseMove(com);
}

void moveL(char ori) {
  String com = "";
  switch (ori) {
    case ' ':   com = "30513150"; break;
    case '\'':  com = "31513050"; break;
    case '2':   com = "32"; break;
  }
  baseMove(com);
}

void moveU(char ori) {
  rotateX(1);
  moveF(ori);
  rotateX(0);
}

void moveUF(char face, char ori) {
  if ((face == 'F' && isxRotate) || (face == 'U' && !isxRotate)) {
    rotateX(1-isxRotate);
    isxRotate = !isxRotate;
  }
    
  // if (face == 'F' && !isxRotate) {
  //   rotateX(0);
  //   isxRotate = true;
  // }

  String com = "";
  switch (ori) {
    case ' ':   com = "00410140"; break;
    case '\'':  com = "01410040"; break;
    case '2':   com = "02"; break;
  }
  baseMove(com);
}

void moveDB(char face, char ori) {
  if ((face == 'B' && isxRotate) || (face == 'D' && !isxRotate)) {
    rotateX(1-isxRotate);
    isxRotate = !isxRotate;
  }

  String com = "";
  switch (ori) {
    case ' ':   com = "20412140"; break;
    case '\'':  com = "21412040"; break;
    case '2':   com = "22"; break;
  }
  baseMove(com);
}

void moveD(char ori) {
  rotateX(1);
  moveB(ori);
  rotateX(0);
}

void rotateX(int ori) {
  String com = "";
  
  //0,2爪子离开
  com += "41";
  //1，3爪子抓紧（防止魔方下坠
  com += "52";
  //1，3爪子旋转
  com += ori? "70" : "71";
  //0，2爪子靠近
  com += "40";
  //1，3爪子离开
  com += "51";
  //1，3爪子旋转
  com += ori? "70" : "71";
  //1，3爪子靠近
  com += "50";

  baseMove(com);
}

void rotateZ(int ori) {
  String com = "";
  
  //1, 3爪子离开
  com += "51";
  //0, 2爪子抓紧（防止魔方下坠
  com += "42";
  //0, 2爪子旋转
  com += ori? "60" : "61";
  //1, 3爪子靠近
  com += "50";
  //0, 2爪子离开
  com += "41";
  //0, 2爪子旋转
  com += ori? "60" : "61";
  //0, 2爪子靠近
  com += "40";

  baseMove(com);
}

/*
void rotateX(int ori) {
  //0,2爪子离开
  stepper[4]->move(step[1][1]);
//  stepper[4]->stop();
//  stepper[4]->runToPosition();
  while (stepper[4]->distanceToGo() != 0) {
    stepper[4]->setSpeed(SPEED);
//      Serial.println(stepper[id]->speed());
    stepper[4]->runSpeedToPosition();
  }

  //1，3爪子抓紧（防止魔方下坠
  stepper[5]->move(-50);
//  stepper[5]->stop();
//  stepper[5]->runToPosition();
  while (stepper[5]->distanceToGo() != 0) {
    stepper[5]->setSpeed(SPEED);
//      Serial.println(stepper[id]->speed());
    stepper[5]->runSpeedToPosition();
  }
  
  //1，3爪子旋转
  position[0] = stepper[1]->currentPosition() + step[0][1-ori];
  position[1] = stepper[3]->currentPosition() + step[0][ori];
  multiStepper[1].moveTo(position);
  multiStepper[1].runSpeedToPosition();
  //0，2爪子靠近
  stepper[4]->move(step[1][0]);
//  stepper[4]->stop();
//  stepper[4]->runToPosition();
  while (stepper[4]->distanceToGo() != 0) {
    stepper[4]->setSpeed(SPEED);
//      Serial.println(stepper[id]->speed());
    stepper[4]->runSpeedToPosition();
  }
  //1，3爪子离开
  stepper[5]->move(step[1][1]);
//  stepper[5]->stop();
//  stepper[5]->runToPosition();
  while (stepper[5]->distanceToGo() != 0) {
    stepper[5]->setSpeed(SPEED);
//      Serial.println(stepper[id]->speed());
    stepper[5]->runSpeedToPosition();
  }
  //1，3爪子旋转
  position[0] = stepper[1]->currentPosition() - step[0][1-ori];
  position[1] = stepper[3]->currentPosition() - step[0][ori];
  multiStepper[1].moveTo(position);
  multiStepper[1].runSpeedToPosition();
  //1，3爪子靠近
  stepper[5]->move(step[1][0]);
//  stepper[5]->stop();
//  stepper[5]->runToPosition();
  while (stepper[5]->distanceToGo() != 0) {
    stepper[5]->setSpeed(SPEED);
//      Serial.println(stepper[id]->speed());
    stepper[5]->runSpeedToPosition();
  }
}
*/

void baseMove(String com) {
  //每一对参数第一个表示电机序号(0-5)，第二个表示转动角度(90,180,-90)
  //序号6（7）代表爪子0，2（1，3）整体旋转，需要用到另一个类，故额外处理
  for (int i = 0; i < com.length() - 1; i += 2) {
    int id = com[i] - '0';
    int ori = com[i+1] - '0';
    if (id < 6) {
      int e = 0;
      if (id < 4) {
        // offset[id] = -offset[id] + (ori? -5 : 5);
        e = offset[ori] - totalError[id];
        if (ori == 2) totalError[id] = offset[ori];
        else totalError[id] = 0;
      }
      stepper[id]->move(step[id / 4][ori] + e);
      // while (stepper[id]->run());
      // Serial.println(e);
      // Serial.println(step[id / 4][ori] + e);
  //    stepper[id]->runToPosition();
  //    Serial.println(stepper[id]->speed());
  //    stepper[id]->stop();
  
      while (stepper[id]->distanceToGo() != 0) {
        stepper[id]->setSpeed(SPEED);
        stepper[id]->runSpeedToPosition();
      }
    } else {
      if (id == 6) {
        position[0] = stepper[0]->currentPosition() - step[0][ori];
        position[1] = stepper[2]->currentPosition() + step[0][ori];
      } else {
        position[0] = stepper[1]->currentPosition() - step[0][ori];
        position[1] = stepper[3]->currentPosition() + step[0][ori];
      }
      multiStepper[id%2].moveTo(position);
      multiStepper[id%2].runSpeedToPosition();
    }
  }
}
