const router = require("express").Router();
const { protect, protectAdmin, protectEditor } = require("../middlewares/authMiddleware");

const {
  create,
  getAll,
  getById,
  update,
  updateStatus,
  remove
} = require("../controllers/ReportUserController");

router
  .route("/")
  .get(protectAdmin, getAll)
  .post(protect, create);

router
  .route("/:id")
  .get(protectAdmin, getById)
  .put(protectAdmin, update)
  .delete(protectAdmin, remove);

router
  .route("/:id/status")
  .patch(protectAdmin, updateStatus);

module.exports = router;
