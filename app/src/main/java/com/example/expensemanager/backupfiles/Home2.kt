package com.example.expensemanager.backupfiles

//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.constraintlayout.compose.ConstraintLayout
//import androidx.constraintlayout.compose.Dimension
//import com.example.expensemanager.ui.theme.green
//
//@Preview(showBackground = true)
//@Composable
//fun samplehome() {
//    Surface(modifier = Modifier.fillMaxSize()) {
//        ConstraintLayout (modifier = Modifier.fillMaxSize()){
//            val (nameRow,list,card,topBar,button)=createRefs()
//            Image(painter = painterResource(id = R.drawable.topbar), contentDescription = null, modifier = Modifier.constrainAs(topBar){
//                top.linkTo(parent.top)
//                start.linkTo(parent.start)
//                end.linkTo(parent.end)
//            })
//
//            Box(modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 32.dp, start = 16.dp, end = 16.dp)
//                .constrainAs(nameRow) {
//                    top.linkTo(parent.top)
//                    start.linkTo(parent.start)
//                    end.linkTo(parent.end)
//
//                }){
//                Column {
//                    Text(text = "FINO", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
//                    Text(text = "Hello,Welcome", fontSize = 16.sp, color = Color.White)
//
//                }
//                Image(painter = painterResource(id = R.drawable.notification), contentDescription = "", modifier = Modifier
//                    .align(Alignment.CenterEnd)
//                    .width(35.dp)
//                )
//            }
//
//
//            CardItem(modifier = Modifier
//                .padding(top = 16.dp)
//                .constrainAs(card) {
//                    top.linkTo(nameRow.bottom)
//                    start.linkTo(parent.start)
//                    end.linkTo(parent.end)
//
//                })
//            TransactionList(modifier = Modifier
//                .fillMaxWidth()
//                .constrainAs(list) {
//                    top.linkTo(card.bottom)
//                    start.linkTo(parent.start)
//                    end.linkTo(parent.end)
//                    bottom.linkTo(parent.bottom)
//                    height = Dimension.fillToConstraints
//                })
//            Button(onClick = {  },modifier = Modifier
//                .padding(16.dp)
//                .height(100.dp)
//                .width(100.dp)
//                .constrainAs(button) {
//                    end.linkTo(parent.end)
//                    bottom.linkTo(parent.bottom)
//                }) {
//
//                Text(text = "+", fontSize = 40.sp, fontWeight = FontWeight.Bold)
//
//
//            }
//        }
//    }
//}
//
//@Composable
//fun CardItem(modifier: Modifier){
//    Column(modifier= modifier
//
//        .padding(16.dp)
//        .fillMaxWidth()
//        .height(200.dp)
//        .clip(RoundedCornerShape(16.dp))
//        .background(green)
//        .padding(16.dp)) {
//        Box(modifier = Modifier
//            .fillMaxWidth()
//            .weight(1f)){
//            Column {
//                Text(text = "Total Balance", fontSize = 16.sp)
//                Text(text="$1000", fontSize = 20.sp, fontWeight = FontWeight.Bold)
//
//            }
//            Image(painter = painterResource(id = R.drawable.list), contentDescription = "", modifier = Modifier
//                .align(Alignment.CenterEnd)
//                .width(20.dp)
//            )
//
//        }
//        Box(modifier= modifier
//            .fillMaxWidth()
//            .weight(1f)){
//            Column(modifier=Modifier.align(Alignment.CenterStart)) {
//                Row{
//                    Image(painter = painterResource(id =R.drawable.income ),
//                        contentDescription = ""
//                    )
//                    Spacer(modifier=Modifier.size(10.dp))
//                    Text(text = "Income", fontSize = 16.sp, color = Color.White)
//                }
//                Text(text = "$500", fontSize = 20.sp,color=Color.White)
//
//            }
//            Column(modifier=Modifier.align(Alignment.CenterEnd)) {
//                Row{
//                    Image(painter = painterResource(id =R.drawable.expense ),
//                        contentDescription = ""
//                    )
//                    Spacer(modifier=Modifier.size(10.dp))
//                    Text(text = "Expense", fontSize = 16.sp, color = Color.White)
//                }
//                Text(text = "$500", fontSize = 20.sp,color=Color.White)
//
//            }
//        }
//
//    }
//
//}
//
//
//@Composable
//fun TransactionList(modifier: Modifier){
//    Column(modifier=modifier){
//        Box (modifier=Modifier.fillMaxWidth()){
//            Text(text = "Recent Transactions", fontSize = 20.sp)
//            Text(text="See all", fontSize = 16.sp,modifier=Modifier.align(Alignment.CenterEnd))
//        }
//        TransactionItem(title = "Food", amount ="$400" , icon =R.drawable.profile , date = "Today", color =Color.Red )
//
//
//    }
//}
//@Composable
//fun TransactionItem(title:String,amount:String,icon:Int,date:String,color:Color){
//    Box(modifier=Modifier.fillMaxWidth()){
//        Row {
//            Image(painter = painterResource(id = icon), contentDescription ="" ,modifier=Modifier.size(50.dp))
//            Spacer(modifier = Modifier.size(8.dp))
//            Column {
//                Text(text = title, fontSize = 16.sp)
//                Text(text = date, fontSize = 12.sp)
//            }
//        }
//        Text(text = amount, fontSize = 20.sp,modifier=Modifier.align(Alignment.CenterEnd),color=color)
//
//    }
//
//
//}
