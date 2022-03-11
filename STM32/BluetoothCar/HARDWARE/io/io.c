#include "io.h"

void LED_Init(void)
{
 GPIO_InitTypeDef  GPIO_InitStructure;
 RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA|RCC_APB2Periph_GPIOC, ENABLE);
 GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3|GPIO_Pin_4|GPIO_Pin_5|GPIO_Pin_6;			
 GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP; 		 //�������
 GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;		 //IO���ٶ�Ϊ50MHz
 GPIO_Init(GPIOA, &GPIO_InitStructure);					 //�����趨������ʼ��
	
 GPIO_InitStructure.GPIO_Pin = GPIO_Pin_13;
 GPIO_Init(GPIOC, &GPIO_InitStructure);					 //�����趨������ʼ��
	
 GPIO_ResetBits(GPIOA,GPIO_Pin_3|GPIO_Pin_4|GPIO_Pin_5|GPIO_Pin_6);
 GPIO_SetBits(GPIOC,GPIO_Pin_13);
	
}
 