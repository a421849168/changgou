package com.changgou.goods.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.Serializable;

/****
 * @Author:Steven
 * @Description:Album构建
 * @Date 2019/6/14 19:13
 *****/
@ApiModel(description = "Album",value = "Album")
@Table(name="tb_album")
public class Album implements Serializable{

	@ApiModelProperty(value = "编号",required = false)
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
	private Long id;//编号
	@ApiModelProperty(value = "相册名称",required = false)
    @Column(name = "title")
	private String title;//相册名称
	@ApiModelProperty(value = "相册封面",required = false)
    @Column(name = "image")
	private String image;//相册封面
	@ApiModelProperty(value = "图片列表",required = false)
    @Column(name = "image_items")
	private String imageItems;//图片列表

	@Override
	public String toString() {
		return "Album{" +
				"id=" + id +
				", title='" + title + '\'' +
				", image='" + image + '\'' +
				", imageItems='" + imageItems + '\'' +
				'}';
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImageItems() {
		return imageItems;
	}

	public void setImageItems(String imageItems) {
		this.imageItems = imageItems;
	}
}
