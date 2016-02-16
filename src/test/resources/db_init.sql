create database record_test;
use record_test;

create table user
(
	id int unsigned auto_increment,
	name varchar(50) default null,
	age int default null,

	primary key (id)
);

create table student
(
	id int unsigned auto_increment,
	name varchar(50) default null,
	depart varchar(50) default null,

	primary key (id)
);

create table name
(
	id int unsigned auto_increment,
	value varchar(50) not null,
	text varchar(50) not null,

	primary key (id)
);

create table info
(
	id int unsigned auto_increment,
	info varchar(50) not null,
	user_id int unsigned not null comment '外键',

	primary key (id),
	foreign key (user_id) references user (id) on delete cascade on update cascade
);

#insert into user(name, age) values('abcd', 11),('qwer', 30),('qindeyu',21),('sunwenqin',20);

#insert into student(name,depart) values('qindeyu','ise'),('sunwenqin','ise');

# 创建视图
create view v_user as
	select user.name, user.age, student.depart, user.id as user_id, student.id as stu_id from
	user
	join student on user.name=student.name
;