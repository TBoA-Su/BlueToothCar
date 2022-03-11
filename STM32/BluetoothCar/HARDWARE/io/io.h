#ifndef __IO_H
#define __IO_H	 
#include "sys.h"

#define IO1 PAout(3)
#define IO2 PAout(4)
#define IO3 PAout(5)
#define IO4 PAout(6)
#define LED13 PCout(13)

void LED_Init(void);

#endif
