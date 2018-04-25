package interfaceApplication;

import java.util.ArrayList;
import java.util.List;

import common.java.json.JSONArray;
import common.java.json.JSONObject;
import org.bson.types.ObjectId;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.authority.plvDef.plvType;
import common.java.interfaceModel.GrapeDBDescriptionModel;
import common.java.interfaceModel.GrapePermissionsModel;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.nlogger.nlogger;
import common.java.session.session;
import common.java.string.StringHelper;

public class Menu {
    private GrapeTreeDBModel menu;
    private GrapeDBDescriptionModel gDbSpecField;
    private GrapePermissionsModel permissionsModel;
    private session se;
    private JSONObject userInfo = null;
    private String userUgid = null;
    private String pkString = null;

    public Menu() {

        menu = new GrapeTreeDBModel();
        
        gDbSpecField = new GrapeDBDescriptionModel();
        gDbSpecField.importDescription(appsProxy.tableConfig("menu"));
        menu.descriptionModel(gDbSpecField);
        
        permissionsModel = new GrapePermissionsModel();
        permissionsModel.importDescription(appsProxy.tableConfig("menu"));
        menu.permissionsModel(permissionsModel);
        
        menu.enableCheck();
        
        pkString = menu.getPk();

        se = new session();
        userInfo = se.getDatas();
        if (userInfo != null && userInfo.size() > 0) {
            userUgid = userInfo.getString("ugid"); // 角色id
        }
    }

    /**
     * 新增菜单
     * 
     * @project GrapeMenu
     * @package interfaceApplication
     * @file Menu.java
     * 
     * @param mString
     *            待操作数据
     * @return {"message":"新增菜单成功","errorcode":0} 或
     *         {"message":"其他异常","errorcode":99}
     *
     */
    public String AddMenu(String mString) {
        JSONObject object = null;
        String result = rMsg.netMSG(100, "新增失败");
        try {
            if (StringHelper.InvaildString(mString)) {
                return rMsg.netMSG(1, "参数错误");
            }
            object = JSONObject.toJSON(mString);
            object = Add(object);
        } catch (Exception e) {
            nlogger.logout(e);
            object = null;
        }
        result = (object != null && object.size() > 0) ? rMsg.netMSG(0, "新增成功", object) : result;
        return result;
    }

    /**
     * 新增操作
     * 
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject Add(JSONObject object) {
        String name = "", id = "", prvid = "";
        JSONObject menuObj = null;
        if (object != null && object.size() > 0) {
            if (object.containsKey("name")) {
                name = object.getString("name");
            }
            JSONObject temp = findByName(name);
            if (temp != null && temp.size() > 0) {
                menuObj = findMenu(name, userUgid);
                if (menuObj == null || menuObj.size() <= 0) {
                    id = temp.getMongoID(pkString);
                    prvid = temp.getString("prvid");
                    prvid = prvid + "," + userUgid;
                    object.put("prvid", prvid);
                    menu.eq(pkString, id).data(object).updateEx();
                }
            } else {
//                JSONObject rMode = new JSONObject(plvType.chkType, plvType.powerVal).puts(plvType.chkVal, 100);// 设置默认查询权限
//                JSONObject uMode = new JSONObject(plvType.chkType, plvType.powerVal).puts(plvType.chkVal, 200);
//                JSONObject dMode = new JSONObject(plvType.chkType, plvType.powerVal).puts(plvType.chkVal, 300);
//                object.put("rMode", rMode.toJSONString()); // 添加默认查看权限
//                object.put("uMode", uMode.toJSONString()); // 添加默认修改权限
//                object.put("dMode", dMode.toJSONString()); // 添加默认删除权限
                object.put("prvid", userUgid);
                id = (String) menu.data(object).insertEx();
            }
        }
        return find(id);
    }

    /**
     * 根据菜单名称和角色id验证菜单是否已存在
     * 
     * @param menuName
     * @param //ugid
     * @return
     */
    private JSONObject findByName(String menuName) {
        JSONObject object = null;
        object = menu.eq("name", menuName).find();
        return object;
    }

    /**
     * 根据菜单名称和角色id验证菜单是否已存在
     * 
     * @param menuName
     * @param ugid
     * @return
     */
    private JSONObject findMenu(String menuName, String ugid) {
        JSONObject object = null;
        object = menu.eq("name", menuName).like("prvid", ugid).find();
        return object;
    }

    /**
     * 根据菜单名称和角色id验证菜单是否已存在
     */
    private JSONObject find(String mid) {
        JSONObject object = null;
        if (StringHelper.InvaildString(mid)) {
            object = menu.eq(pkString, mid).find();
        }
        return object;
    }

    /**
     * 修改菜单
     * 
     * @project GrapeMenu
     * @package interfaceApplication
     * @file Menu.java
     * 
     * @param mString
     *            待操作数据
     * @return {"message":"新增菜单成功","errorcode":0} 或
     *         {"message":"其他异常","errorcode":99}
     *
     */
    public String UpdateMenu(String mid, String mString) {
        boolean tip = false;
        String result = rMsg.netMSG(100, "修改失败");
        try {
            if (StringHelper.InvaildString(mid) || StringHelper.InvaildString(mString)) {
                return rMsg.netMSG(1, "参数错误");
            }
            JSONObject object = JSONObject.toJSON(mString);
            tip = menu.eq(pkString, mid).data(object).updateEx();
        } catch (Exception e) {
            nlogger.logout(e);
        }
        return result = tip ? rMsg.netMSG(0, "修改成功") : result;
    }

    /**
     * 删除菜单
     * 
     * @param id
     * @return
     */
    public String DeleteMenu(String id) {
        return DeleteBatchMenu(id);
    }

    public String DeleteBatchMenu(String id) {
        String[] value = null;
        long code = 0;
        String result = rMsg.netMSG(100, "删除失败");
        try {
            if (StringHelper.InvaildString(id)) {
                return rMsg.netMSG(1, "参数错误");
            }
            value = id.split(",");
            if (value != null) {
                menu.or();
                for (String mid : value) {
                    if (ObjectId.isValid(mid)) {
                        menu.eq(pkString, mid);
                    }
                }
                code = menu.deleteAll();
            }
        } catch (Exception e) {
            nlogger.logout(e);
            code = 0;
        }
        result = code > 0 ? rMsg.netMSG(0, "删除成功") : result;
        return result;
    }

    /**
     * 根据当前用户的角色显示菜单信息
     * 
     * @return
     */
    public String ShowMenu() {
        JSONArray array = null;
        System.out.println("ugid: " + userUgid);
        if (StringHelper.InvaildString(userUgid)) {
            array = menu.eq("state", 0).like("prvid", userUgid).select();
        }
        return rMsg.netMSG(true, (array != null && array.size() > 0) ? array : new JSONArray());
    }

    /**
     * 给菜单设置所属角色id
     * 
     * @project GrapeMenu
     * @package interfaceApplication
     * @file Menu.java
     * 
     * @param //id
     *            角色id
     * @param mid
     *            菜单id
     * @return
     *
     */
    public String SetRole(String rid, String mid) {
        long code = 0;
        String temp, result = rMsg.netMSG(100, "设置所属角色失败");
        if (!StringHelper.InvaildString(rid)) {
            return rMsg.netMSG(1, "无效角色id");
        }
        if (!StringHelper.InvaildString(mid)) {
            return rMsg.netMSG(2, "无效菜单id");
        }
        try {
            String[] mids = mid.split(",");
            if (mids != null) {
                for (String id : mids) {
                    if (code == 0) {
                        temp = setManager(rid, id);
                        code = JSONObject.toJSON(temp).getLong("errorcode");
                    } else {
                        result = rMsg.netMSG(100, "设置失败");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            nlogger.logout(e);
            result = rMsg.netMSG(100, "设置失败");
        }
        return code == 0 ? rMsg.netMSG(0, "设置成功") : result;
    }

    /**
     * 给菜单设置所属角色id
     * 
     * @project GrapeMenu
     * @package interfaceApplication
     * @file Menu.java
     * 
     * @param id
     *            角色id
     * @param mid
     *            菜单id
     * @return
     *
     */
    private String setManager(String rid, String mid) {
        String result = rMsg.netMSG(100, "设置失败");
        JSONObject object = menu.eq(pkString, mid).like("prvid", rid).find();
        if (object != null && object.size() > 0) {
            return rMsg.netMSG(3, "该管理员已具备操作此菜单的权限");
        }
        String prvid = "";
        object = menu.eq(pkString, mid).field("prvid").find();
        if (object != null && object.size() > 0) {
            prvid = object.get("prvid").toString();
            List<String> list = Str2List(prvid);
            list.add(rid);
            prvid = StringHelper.join(list);
        }
        prvid = "{\"prvid\":\"" + prvid + "\"}";
        int code = menu.eq(pkString, mid).data(prvid).updateEx()  ? 0 : 99;
        result = code == 0 ? rMsg.netMSG(0, "设置成功") : rMsg.netMSG(100, "设置失败");
        return result;
    }

    /**
     * 用逗号分隔的字符串转换成list
     * 
     * @project GrapeMenu
     * @package interfaceApplication
     * @file Menu.java
     * 
     * @param iString
     * @return
     *
     */
    private List<String> Str2List(String iString) {
        List<String> list = new ArrayList<String>();
        String[] strings = iString.split(",");
        for (String string : strings) {
            list.add(string);
        }
        return list;
    }
}
