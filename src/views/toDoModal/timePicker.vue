<template>
  <div
    class="header-menu-icons"
    type="button"
    data-bs-toggle="dropdown"
    data-bs-auto-close="outside"
    :title="$t('todoDetails.time')"
  >
    <i
      id="btnTaskTimePicker"
      :class="{ 'bi-alarm': !startTime && !endTime, 'bi-alarm-fill': startTime || endTime }"
    ></i>
  </div>

  <ul
    class="dropdown-menu color-picker-dropdown"
    aria-labelledby="btnTaskTimePicker"
  >
    <div class="d-flex align-items-center mx-3 mb-2">
      <label for="taskStartTime" class="mr-2">开始时间</label>
      <input
        id="taskStartTime"
        type="time"
        v-model="startTime"
        @blur="selectTimeRange()"
      />
    </div>
    <div class="d-flex align-items-center mx-3">
      <label for="taskEndTime" class="mr-2">结束时间</label>
      <input
        id="taskEndTime"
        type="time"
        v-model="endTime"
        @blur="selectTimeRange()"
      />
      <i
        class="header-menu-icons bi-trash"
        type="button"
        @click="clearTime"
        title="清除时间"
      ></i>
    </div>
  </ul>
</template>

<script>
export default {
  name: "timePicker",
  emits: ["timeSelected"],
  data() {
    return {
      startTime: "",
      endTime: ""
    };
  },
  props: {
    time: { required: true, type: [Object, String, null] },
  },
  methods: {
    selectTimeRange() {
      // 构建包含开始和结束时间的对象
      const timeRange = {
        start: this.startTime || null,
        end: this.endTime || null
      };
      this.$emit("timeSelected", timeRange);
    },
    clearTime() {
      this.startTime = null;
      this.endTime = null;
      this.selectTimeRange();
    },
  },
  watch: {
    time: function (newVal) {
      // 处理传入的时间数据
      if (typeof newVal === 'object' && newVal !== null) {
        this.startTime = newVal.start || '';
        this.endTime = newVal.end || '';
      } else {
        // 保持向后兼容，处理字符串格式的时间
        this.startTime = newVal || '';
        this.endTime = '';
      }
    },
  },
};
</script>

<style scoped lang="scss">
@import "/src/assets/style/globalVars.scss";

.header-menu-icons {
  margin-left: 6px;
  @include btn-icon;
}

.bi-trash {
  margin: 0px;
  cursor: pointer;
}

input[type="time"] {
  background-color: transparent;
  border: none;
  font-size: 16px;
  width: 130px;
  outline: unset;
  height: 40px;
}

input[type="time"]::-webkit-datetime-edit-text {
  padding: 19px 2px;
}

input[type="time"]::-webkit-datetime-edit-fields-wrapper {
  /*display: block;*/
  padding: 8px 2px 8px 2px;
  border: none;
}

input[type="time"]::-webkit-datetime-edit-hour-field,
input[type="time"]::-webkit-datetime-edit-minute-field,
input[type="time"]::-webkit-datetime-edit-ampm-field {
  background-color: transparent;
  border: 2px solid transparent;
  border-radius: 5px;
  padding: 5px;
  min-width: 80px;
  width: 80px;
  color: #494949;

  .dark-theme & {
    color: #bfbfbf;
  }
}

input[type="time"]::-webkit-calendar-picker-indicator {
  background: none;
  display: none;
}

input[type="time"]::-webkit-datetime-edit-hour-field:hover,
input[type="time"]::-webkit-datetime-edit-minute-field:hover,
input[type="time"]::-webkit-datetime-edit-ampm-field:hover {
  color: black;
  background-color: #f4f4f4;

  .dark-theme & {
    color: white;
    background-color: #303940;
  }
}

input[type="time"]::-webkit-datetime-edit-hour-field:focus,
input[type="time"]::-webkit-datetime-edit-minute-field:focus,
input[type="time"]::-webkit-datetime-edit-ampm-field:focus {
  border: 2px solid black;
  color: black;
  background-color: transparent;

  .dark-theme & {
    border: 2px solid white;
    color: white;
  }
}
</style>