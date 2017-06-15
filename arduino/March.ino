#include <AccelStepper.h>
#include <MultiStepper.h>

#define SPEED 1000
#define ACCELERATION 900

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

String comdata = "";
int step[2][3] = {400, -400, 800,
                  -500, 450, 800};
long position[2];

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  for (int i = 0; i < 6; i++) {
    stepper[i]->setMaxSpeed(SPEED);
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
  // put your main code here, to run repeatedly:
  getCom();
  exeCom();
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
  if (comdata == "") return;
  solve();
  comdata = "";
}

void solve() {
  for (int i = 0; i < comdata.length(); i++) {
    if (comdata[i] == '\'' || comdata[i] == '2')
      continue;
    char ori = ' ';
    if (i != comdata.length() - 1
        && (comdata[i+1] == '\'' || comdata[i+1] == '2'))
      ori = comdata[i+1];
    Serial.println(comdata[i]);
    Serial.println(ori);
    move(comdata[i], ori);
  }
}

void move(char face, char ori) {
  switch (face) {
    case 'U': moveU(ori); break;
    case 'F': moveF(ori); break;
    case 'R': moveR(ori); break;
    case 'B': moveB(ori); break;
    case 'L': moveL(ori); break;
    case 'D': moveD(ori); break;
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
  moveY(0);
  moveF(ori);
  moveY(1);
}

void moveD(char ori) {
  moveY(0);
  moveB(ori);
  moveY(1);
}

void moveY(int ori) {
  //0,2爪子离开
  stepper[4]->move(step[1][1]);
//  stepper[4]->stop();
//  stepper[4]->runToPosition();
  while (stepper[4]->distanceToGo() != 0) {
    stepper[4]->setSpeed(SPEED);
//      Serial.println(stepper[id]->speed());
    stepper[4]->runSpeedToPosition();
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

void baseMove(String com) {
  for (int i = 0; i < com.length() - 1; i += 2) {
    int id = com[i] - '0';
    int ori = com[i+1] - '0';
    stepper[id]->move(step[id / 4][ori]);
    // while (stepper[id]->run());
//    Serial.println(stepper[id]->currentPosition());
//    stepper[id]->runToPosition();
//    Serial.println(stepper[id]->speed());
//    stepper[id]->stop();
    while (stepper[id]->distanceToGo() != 0) {
      stepper[id]->setSpeed(SPEED);
//      Serial.println(stepper[id]->speed());
      stepper[id]->runSpeedToPosition();
    }
  }
}
