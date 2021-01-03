package com.cn.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 2131231231233434L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 225)
    private String name;

    @Column(name = "pass", length = 225)
    private String pass;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
